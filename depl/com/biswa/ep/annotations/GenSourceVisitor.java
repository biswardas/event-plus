package com.biswa.ep.annotations;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor6;

import com.biswa.ep.annotations.EPContainer.Transaction;
import com.sun.tools.javac.code.Symbol;

public class GenSourceVisitor extends SimpleElementVisitor6<Void, Void> {
	private Writer writer = null;
	private String context = null;
	private ProxyManager proxyManager = new ProxyManager();

	public GenSourceVisitor() {
		writer = new OutputStreamWriter(System.out);
	}

	public GenSourceVisitor(Writer writer) {
		this.writer = writer;
	}

	@Override
	public Void visitType(TypeElement e, Void v) {
		Annotation annot = e.getAnnotation(EPContext.class);
		if (annot != null) {
			context = e.getSimpleName().toString();
			write("<?xml version='1.0'?>");
			write("<Context name='" + context
					+ "' xmlns='http://www.biswadas.com/pp/deployment-desc'>");
			visitContainers(e);
			write("</Context>");
		}
		return null;
	}

	private void visitContainers(TypeElement epContext) {
		for (Element innerElement : epContext.getEnclosedElements()) {
			EPContainer containerAnnot = innerElement
					.getAnnotation(EPContainer.class);
			if (containerAnnot != null) {
				TypeElement epContainer = (TypeElement) innerElement;
				String containerName = epContainer.getSimpleName().toString();
				TransactionGrouper tranGrouper = new TransactionGrouper(
						epContainer);
				write("<Container type='" + containerAnnot.type() + "' name='"
						+ containerName + "'>");
				write("<Publish method='" + containerAnnot.publish() + "'/>");
				String contextToListen = containerAnnot.context().isEmpty() ? context
						: containerAnnot.context();
				switch (containerAnnot.type()) {
				case Proxy:
					proxyManager.registerProxy(epContainer);

					writeListener(tranGrouper.genRegTranGrp(containerName),
							containerAnnot.publish(),
							containerAnnot.container(), contextToListen);

					writeSubscriber(containerAnnot.container(),
							contextToListen, containerAnnot.publish(),
							"SUBJECT", "Proxy");
					break;
				default:
					if (epContainer.getSuperclass().getKind() != TypeKind.NONE) {
						Element classElement = ((DeclaredType) epContainer
								.getSuperclass()).asElement();
						if (classElement.getAnnotation(EPContainer.class) != null) {
							generateListener(epContext, epContainer,
									classElement,
									tranGrouper.genRegTranGrp(classElement));
						}
					}
					for (TypeMirror typeMirror : epContainer.getInterfaces()) {
						Element interfaceElement = ((DeclaredType) typeMirror)
								.asElement();
						if (interfaceElement.getAnnotation(EPContainer.class) != null) {
							generateListener(epContext, epContainer,
									interfaceElement,
									tranGrouper.genRegTranGrp(interfaceElement));
						}
					}
					visitAttributes(epContainer, tranGrouper);

				}
				if (!containerAnnot.generator().isEmpty()) {
					write("<Source className='"+((Symbol.ClassSymbol)epContainer).flatName()+"$Inlet'/>");
				}
				for (String oneParam : containerAnnot.params()) {
					write("<Param Name='" + getKey(oneParam) + "' Value='"
							+ getValue(oneParam) + "'/>");
				}
				write("</Container>");
			}
		}
	}

	private void visitAttributes(Element epContainer,
			TransactionGrouper tranGrouper) {
		for (Element innerElement : epContainer.getEnclosedElements()) {
			EPAttribute epAttribute = innerElement
					.getAnnotation(EPAttribute.class);
			if (epAttribute != null) {
				Symbol.ClassSymbol tElement = (Symbol.ClassSymbol) innerElement;
				writeAttribute(tElement.flatName().toString());
				if (epAttribute.type() == EPAttrType.Subscriber) {
					String sourceContext = epAttribute.context().isEmpty() ? context
							: epAttribute.context();
					EPPublish listenMethod = sourceContext == context ? EPPublish.LOCAL
							: epContainer.getAnnotation(EPContainer.class)
									.publish();
					// What about listening same container multiple times from
					// same entry
					if (!tranGrouper.isRegistered(epAttribute.container())) {
						if (sourceContext == context
								&& proxyManager.isProxy(epAttribute.container())) {
							TypeElement proxyContainer = proxyManager
									.getProxy(epAttribute.container());
							EPContainer proxyAnnotation = proxyContainer
									.getAnnotation(EPContainer.class);
							String actualContext = proxyAnnotation.context().isEmpty() ? context
									: proxyAnnotation.context();
							if(!tranGrouper.isRegistered(proxyAnnotation.container())){
								writeListener(tranGrouper.genRegTranGrp(proxyAnnotation.container()), listenMethod,
										epAttribute.container(), sourceContext);	
								writeFeedback(proxyAnnotation.publish(), proxyAnnotation.container(),
										actualContext);
							}else{
								writeListener(tranGrouper.genRegTranGrp(proxyAnnotation.container()), listenMethod,
										epAttribute.container(), sourceContext);
							}
						}else{
							writeListener(tranGrouper.genRegTranGrp(epAttribute
									.container()), listenMethod,
									epAttribute.container(), sourceContext);	
							writeFeedback(listenMethod, epAttribute.container(),
									sourceContext);						
						}
					}

					writeSubscriber(epAttribute.container(), sourceContext,
							listenMethod, epAttribute.depends(), innerElement
									.getSimpleName().toString());
				}
			}
		}
	}

	private String getKey(String oneParam) {
		return oneParam.substring(0, oneParam.indexOf('='));
	}

	private String getValue(String oneParam) {
		return oneParam.substring(oneParam.indexOf('=') + 1);
	}

	private void generateListener(Element epContext, Element epContainer,
			Element inheritedContainer, int tranGroup) {
		boolean supportsFeedback = false;
		EPContainer containerAnnot = inheritedContainer
				.getAnnotation(EPContainer.class);
		supportsFeedback = containerAnnot.type().supportsFeedback();
		EPPublish listenMethod = EPPublish.LOCAL;
		if (!epContext.getEnclosedElements().contains(inheritedContainer)) {
			listenMethod = containerAnnot.publish();
		}
		String containerToListen = inheritedContainer.getSimpleName()
				.toString();
		String contextToListen = inheritedContainer.getEnclosingElement()
				.getSimpleName().toString();
		writeListener(tranGroup, listenMethod, containerToListen,
				contextToListen);
		if (supportsFeedback) {
			writeFeedback(listenMethod, containerToListen, contextToListen);
		}
	}

	private void writeListener(int tranGroup, EPPublish listenMethod,
			String containerToListen, String contextToListen) {
		write("<Listen container='" + containerToListen + "' context='"
				+ contextToListen + "' method='" + listenMethod
				+ "' transactionGroup='" + tranGroup + "'/>");
	}

	private void writeAttribute(String string) {
		write("<Attribute className='" + string + "'/>");
	}

	private void writeFeedback(EPPublish listenMethod,
			String containerToListen, String contextToListen) {
		write("<Feedback container='" + containerToListen + "' context='"
				+ contextToListen + "' method='" + listenMethod + "'/>");
	}

	private void writeSubscriber(String container, String context,
			EPPublish listenMethod, String depends, String response) {
		write("<Subscribe container='" + container + "' context='" + context
				+ "' method='" + listenMethod + "' depends='" + depends
				+ "' response='" + response + "'/>");
	}

	private void write(String string) {
		try {
			writer.write(string);
			//System.out.println(string);
			writer.write("\n");
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}

class TransactionGrouper {
	private Transaction[] tran;
	private int count;
	private HashMap<String, Integer> containerTrangroupMap = new HashMap<String, Integer>();

	public TransactionGrouper(TypeElement epContainer) {
		EPContainer epContainerAnn = epContainer
				.getAnnotation(EPContainer.class);
		tran = epContainerAnn.transaction();
	}

	public int genRegTranGrp(Element inheritedContainer) {
		String containerName = inheritedContainer.getSimpleName().toString();
		if (!containerTrangroupMap.containsKey(containerName)) {
			int tranGroup = 0;
			for (int i = 0; i < tran.length; i++) {
				for (String oneContainer : tran[i].group()) {
					if (oneContainer.equals(containerName)) {
						tranGroup = tranGroup | (1 << i);
					}
				}
			}
			containerTrangroupMap.put(containerName, tranGroup == 0 ? 1
					: tranGroup);
		}
		return containerTrangroupMap.get(containerName);
	}

	/**
	 * Used by the subscribers.
	 * 
	 * @return
	 */
	public int genRegTranGrp(String containerName) {
		if (!containerTrangroupMap.containsKey(containerName)) {
			containerTrangroupMap.put(containerName,
					1 << (tran.length + ++count));
		}
		return containerTrangroupMap.get(containerName);
	}

	/*
	 * Check if transaction group already generated for this
	 */
	public boolean isRegistered(String containerName) {
		return containerTrangroupMap.containsKey(containerName);
	}
}

class ProxyManager {
	private HashMap<String, TypeElement> proxies = new HashMap<String, TypeElement>();

	public void registerProxy(TypeElement epContainer) {
		proxies.put(epContainer.getSimpleName().toString(), epContainer);
	}

	public TypeElement getProxy(String name) {
		return proxies.get(name);
	}

	public boolean isProxy(String name) {
		return proxies.containsKey(name);
	}
}