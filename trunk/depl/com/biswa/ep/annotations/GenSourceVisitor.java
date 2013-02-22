package com.biswa.ep.annotations;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.biswa.ep.annotations.EPContainer.Transaction;
import com.sun.javadoc.AnnotationValue;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Context;

public class GenSourceVisitor implements
		ElementVisitor<Object, HashMap<Object, Object>> {
	private Writer writer = null;
	private String context = null;

	public GenSourceVisitor() {
		writer = new OutputStreamWriter(System.out);
	}

	public GenSourceVisitor(Writer writer) {
		this.writer = writer;
	}

	@Override
	public Object visit(Element e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Element e, HashMap<Object, Object> p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitExecutable(ExecutableElement e, HashMap<Object, Object> p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitPackage(PackageElement e, HashMap<Object, Object> p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitType(TypeElement e, HashMap<Object, Object> p) {
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
						writeListeners(epContext, epContainer, classElement);
					}
				}
				for (TypeMirror typeMirror : epContainer.getInterfaces()) {
					Element interfaceElement = ((DeclaredType) typeMirror)
							.asElement();
					writeListeners(epContext, epContainer, interfaceElement);
				}
				visitAttributes(innerElement, epContainer);
				write("</Container>");
			}
		}
	}

	private void writeListeners(Element epContext, Element epContainer,
			Element inheritedContainer) {
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
				+ "' transactionGroup='"
				+ tranGroup(epContainer, inheritedContainer) + "'/>");
		if (supportsFeedback) {
			write("<Feedback container='" + containerToListen + "' context='"
					+ contextToListen + "' method='" + listenMethod + "'/>");
		}
	}

	// TODO How to use Fully Qualified container name
	private int tranGroup(Element epContainer, Element inheritedContainer) {
		int tranGroup = 0;
		EPContainer epContainerAnn = epContainer.getAnnotation(EPContainer.class);
		Transaction[] tran = epContainerAnn.transaction();
		for(int i=0;i<tran.length;i++){
			for(String oneContainer:tran[i].group()){
				if(oneContainer.equals(inheritedContainer.getSimpleName().toString())){
					tranGroup=tranGroup|(1<<i);
				}
			}
		}
		return tranGroup==0?1:tranGroup;
	}

	private void visitAttributes(Element host, Element e) {
		for (Element innerElement : e.getEnclosedElements()) {
			EPAttribute iannot = innerElement.getAnnotation(EPAttribute.class);
			if (iannot != null) {
				Symbol.ClassSymbol tElement = (Symbol.ClassSymbol) innerElement;
				write("<Attribute className='" + tElement.flatName() + "'/>");
				if (iannot.type() == EPAttrType.Subscriber) {
					String sourceContext = iannot.context().isEmpty() ? context
							: iannot.context();
					String listenMethod = sourceContext == context ? "LOCAL"
							: host.getAnnotation(EPContainer.class).publish();
					write("<Listen container='" + iannot.container()
							+ "' context='" + sourceContext + "' method='"
							+ listenMethod + "'/>");
					write("<Feedback container='" + iannot.container()
							+ "' context='" + sourceContext + "' method='"
							+ listenMethod + "'/>");
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

	@Override
	public Object visitTypeParameter(TypeParameterElement e,
			HashMap<Object, Object> p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitUnknown(Element e, HashMap<Object, Object> p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitVariable(VariableElement e, HashMap<Object, Object> p) {
		// TODO Auto-generated method stub
		return null;
	}

}
