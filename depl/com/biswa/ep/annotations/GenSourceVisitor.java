package com.biswa.ep.annotations;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor6;

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
				UpstreamContainerManager upStreamContainers = new UpstreamContainerManager();
				write("<Container type='" + containerAnnot.type() + "' name='"
						+ containerName + "'>");
				write("<Publish method='" + containerAnnot.publish() + "'/>");
				String contextToListen = containerAnnot.context().isEmpty() ? context
						: containerAnnot.context();
				switch (containerAnnot.type()) {
				case Proxy:
					proxyManager.registerProxy(epContainer);
					upStreamContainers.register(containerName);
					writeListener(
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
							upStreamContainers.register(classElement);
							generateListener(epContext, epContainer,
									classElement);
						}
					}
					for (TypeMirror typeMirror : epContainer.getInterfaces()) {
						Element interfaceElement = ((DeclaredType) typeMirror)
								.asElement();
						if (interfaceElement.getAnnotation(EPContainer.class) != null) {
							upStreamContainers.register(interfaceElement);
							generateListener(epContext, epContainer,
									interfaceElement);
						}
					}
					visitAttributes(epContainer, upStreamContainers);

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
			UpstreamContainerManager upStreamContainers) {
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
					if (!upStreamContainers.isRegistered(epAttribute.container())) {
						if (sourceContext == context
								&& proxyManager.isProxy(epAttribute.container())) {
							TypeElement proxyContainer = proxyManager
									.getProxy(epAttribute.container());
							EPContainer proxyAnnotation = proxyContainer
									.getAnnotation(EPContainer.class);
							String actualContext = proxyAnnotation.context().isEmpty() ? context
									: proxyAnnotation.context();
							if(!upStreamContainers.isRegistered(proxyAnnotation.container())){
								upStreamContainers.register(proxyAnnotation.container());
								writeListener(listenMethod,
										epAttribute.container(), sourceContext);	
								writeFeedback(proxyAnnotation.publish(), proxyAnnotation.container(),
										actualContext);
							}else{
								upStreamContainers.register(proxyAnnotation.container());
								writeListener(listenMethod,
										epAttribute.container(), sourceContext);
							}
						}else{
							upStreamContainers.register(epAttribute
								.container());
							writeListener(listenMethod,
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
			Element inheritedContainer) {
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
		writeListener(listenMethod, containerToListen,
				contextToListen);
		if (supportsFeedback) {
			writeFeedback(listenMethod, containerToListen, contextToListen);
		}
	}

	private void writeListener(EPPublish listenMethod,
			String containerToListen, String contextToListen) {
		write("<Listen container='" + containerToListen + "' context='"
				+ contextToListen + "' method='" + listenMethod
				+ "'/>");
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

class UpstreamContainerManager {
	private HashSet<String> registrations = new HashSet<String>();
	public void register(Element inheritedContainer) {
		register(inheritedContainer.getSimpleName().toString());
	}
	/**
	 * Used by the subscribers.
	 * 
	 * @return
	 */
	public void register(String containerName) {
		registrations.add(containerName);
	}
	/*
	 * Check if transaction group already generated for this
	 */
	public boolean isRegistered(String containerName) {
		return registrations.contains(containerName);
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