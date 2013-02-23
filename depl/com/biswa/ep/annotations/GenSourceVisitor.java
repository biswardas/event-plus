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

import com.biswa.ep.annotations.EPContainer.Feedback;
import com.biswa.ep.annotations.EPContainer.Transaction;
import com.sun.tools.javac.code.Symbol;

public class GenSourceVisitor extends
		SimpleElementVisitor6<Void,Void> {
	private Writer writer = null;
	private String context = null;

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
				TransactionGrouper tranGrouper = new TransactionGrouper(
						epContainer);
				write("<Container type='" + containerAnnot.type() + "' name='"
						+ epContainer.getSimpleName() + "'>");
				if (!containerAnnot.publish().isEmpty()) {
					write("<Publish method='" + containerAnnot.publish()
							+ "'/>");
				}
				if (epContainer.getSuperclass().getKind() != TypeKind.NONE) {
					Element classElement = ((DeclaredType) epContainer
							.getSuperclass()).asElement();
					if (classElement.getAnnotation(EPContainer.class) != null) {
						writeListener(epContext, epContainer, classElement,
								tranGrouper.generateAndRegisterTransactionGroup(classElement));
					}
				}
				for (TypeMirror typeMirror : epContainer.getInterfaces()) {
					Element interfaceElement = ((DeclaredType) typeMirror)
							.asElement();
					if (interfaceElement.getAnnotation(EPContainer.class) != null) {
						writeListener(epContext, epContainer, interfaceElement,
								tranGrouper.generateAndRegisterTransactionGroup(interfaceElement));
					}
				}
				visitAttributes(epContainer, tranGrouper);
				for (Feedback feedback : containerAnnot.feedback()) {
					write("<Feedback container='" + feedback.container()
							+ "' context='" + feedback.context() + "' method='"
							+ feedback.publish() + "'/>");
				}
				for(String oneParam:containerAnnot.params()){
					write("<Param Name='"+getKey(oneParam)+"' Value='"+getValue(oneParam)+"'/>");	
				}
				write("</Container>");
			}
		}
	}

	private String getKey(String oneParam) {
		return oneParam.substring(0,oneParam.indexOf('='));
	}
	private String getValue(String oneParam) {
		return oneParam.substring(oneParam.indexOf('=')+1);
	}

	private void writeListener(Element epContext, Element epContainer,
			Element inheritedContainer, int tranGroup) {
		String listenMethod = "LOCAL";
		boolean supportsFeedback = false;
		EPContainer containerAnnot = inheritedContainer
				.getAnnotation(EPContainer.class);
		supportsFeedback = containerAnnot.type().supportsFeedback();
		if (!epContext.getEnclosedElements().contains(inheritedContainer)) {
			listenMethod = containerAnnot.publish();
		}
		String containerToListen = inheritedContainer.getSimpleName()
				.toString();
		String contextToListen = inheritedContainer.getEnclosingElement()
				.getSimpleName().toString();
		write("<Listen container='" + containerToListen + "' context='"
				+ contextToListen + "' method='" + listenMethod
				+ "' transactionGroup='" + tranGroup + "'/>");
		if (supportsFeedback) {
			write("<Feedback container='" + containerToListen + "' context='"
					+ contextToListen + "' method='" + listenMethod + "'/>");
		}
	}

	private void visitAttributes(Element epContainer,
			TransactionGrouper tranGrouper) {
		for (Element innerElement : epContainer.getEnclosedElements()) {
			EPAttribute iannot = innerElement.getAnnotation(EPAttribute.class);
			if (iannot != null) {
				Symbol.ClassSymbol tElement = (Symbol.ClassSymbol) innerElement;
				write("<Attribute className='" + tElement.flatName() + "'/>");
				if (iannot.type() == EPAttrType.Subscriber) {
					String sourceContext = iannot.context().isEmpty() ? context
							: iannot.context();
					String listenMethod = sourceContext == context ? "LOCAL"
							: epContainer.getAnnotation(EPContainer.class)
									.publish();
					// What about listening same container multiple times from
					// same entry
					if(!tranGrouper.isRegistered(iannot.container())){
						write("<Listen container='" + iannot.container()
								+ "' context='" + sourceContext + "' method='"
								+ listenMethod + "' transactionGroup='"
								+ tranGrouper.generateAndRegisterTransactionGroup(iannot.container()) + "'/>");	
					}
					// TODO What about proxy?
					// write("<Feedback container='" + iannot.container()
					// + "' context='" + sourceContext + "' method='"
					// + listenMethod + "'/>");

					write("<Subscribe container='" + iannot.container()
							+ "' context='" + sourceContext + "' method='"
							+ listenMethod + "' depends='" + iannot.depends()
							+ "' response='" + innerElement.getSimpleName()
							+ "'/>");
				}
			}
		}
	}

	private void write(String string) {
		try {
			writer.write(string);
			System.out.println(string);
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

	public int generateAndRegisterTransactionGroup(Element inheritedContainer) {
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
			containerTrangroupMap.put(containerName, tranGroup == 0 ? 1 : tranGroup);
		}
		return containerTrangroupMap.get(containerName);
	}

	/**
	 * Used by the subscribers.
	 * 
	 * @return
	 */
	public int generateAndRegisterTransactionGroup(String containerName) {
		if (!containerTrangroupMap.containsKey(containerName)) {
			containerTrangroupMap.put(containerName,
					1 << (tran.length + ++count));
		}
		return containerTrangroupMap.get(containerName);
	}
	/*
	 * Check if transaction group already generated for this
	 */
	public boolean isRegistered(String containerName){
		return containerTrangroupMap.containsKey(containerName);
	}
}