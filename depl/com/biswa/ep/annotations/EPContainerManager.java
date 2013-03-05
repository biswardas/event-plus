package com.biswa.ep.annotations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import com.biswa.ep.annotations.EPCompilationException.ErrorCode;


public class EPContainerManager {

	private class TypeManager {
		private HashMap<String, HashMap<String, Element>> typeMap = new HashMap<String, HashMap<String, Element>>();

		private Element lookUpType(String sourceClassName, String variable) {
			Element element = null;
			HashMap<String, Element> myMap = typeMap.get(sourceClassName);
			if (myMap != null) {
				element = myMap.get(variable);
			}
			if (element == null) {
				Set<String> parentType = inheritanceTree
						.get(sourceClassName);
				if (parentType != null) {
					for (String oneSuper : parentType) {
						element = lookUpType(oneSuper, variable);
						if (element != null) {
							break;
						}
					}
				}
			}
			return element;
		}

		public void addInheritance(String currentContainerName, String superClassName) {
			if (inheritanceTree.containsKey(currentContainerName)) {
				inheritanceTree.get(currentContainerName).add(
						superClassName);
			} else {
				HashSet<String> hs = new HashSet<String>();
				hs.add(superClassName);
				inheritanceTree.put(currentContainerName, hs);
			}
		}

		public void registerMember(String currentContainerName, String memberName,
				Element element) {
			if (typeMap.containsKey(currentContainerName)) {
				typeMap.get(currentContainerName).put(memberName, element);
			} else {
				HashMap<String, Element> hs = new HashMap<String, Element>();
				hs.put(memberName, element);
				typeMap.put(currentContainerName, hs);
			}
		}
	}

	private HashMap<String, HashSet<String>> inheritanceTree = new HashMap<String, HashSet<String>>();

	private HashMap<String, TypeElement> containers = new HashMap<String, TypeElement>();

	private final TypeManager typeManager = new TypeManager();
	
	public void registerContainer(TypeElement epContainer) {
		containers.put(epContainer.getSimpleName().toString(), epContainer);
	}

	public TypeElement getContainer(String name) {
		return containers.get(name);
	}

	public boolean isProxy(String name) {
		TypeElement element = containers.get(name);
		return element.getAnnotation(EPContainer.class).type() == EPConType.Proxy;
	}
	public boolean supportsFeedback(String name) {
		TypeElement element = containers.get(name);
		return element.getAnnotation(EPContainer.class).type().supportsFeedback();
	}
	
	public EPPublish getListenMethod(String context,String containerName,String sourceContext){
		return sourceContext.equals(context) ? EPPublish.LOCAL
				: containers.get(containerName).getAnnotation(EPContainer.class)
						.publish();
	}

	public String getQualifiedName(String container) {
		return containers.get(container).getQualifiedName().toString();
	}

	public boolean typeKnown(String currentContainerName,String attribute) {
		return (typeManager.lookUpType(currentContainerName, attribute) != null);
	}

	public void addInheritance(String className, String superClassName) {
		typeManager.addInheritance(className, superClassName);
	}

	public void addMember(String className, String memberName, Element element) {
		typeManager.registerMember(className, memberName, element);
	}
	
	public String lookUpType(String currentContainerName,String oneDependency) {
		String type = null;
		Element element = typeManager.lookUpType(currentContainerName, oneDependency);
		switch (element.getKind()) {
			case FIELD:
				VariableElement vael = (VariableElement) element;
				type =  vael.asType().toString();
				break;
			case METHOD:
				ExecutableElement meth = (ExecutableElement) element;
				type =  meth.getReturnType().toString();
			default:
				break;
		}
		return type;
	}
	public EPAttribute getEPAttribute(String currentContainerName,String memberName) {			
		Element element = typeManager.lookUpType(currentContainerName, memberName);
		EPAttribute epAttribute=element.getAnnotation(EPAttribute.class);
		if(!currentContainerName.equals(element.getEnclosingElement().asType().toString())){
			if(element.getModifiers().contains(Modifier.PRIVATE)){
				throw new EPCompilationException("Access privilage violation in container :"+currentContainerName+" member:"+memberName,ErrorCode.ACCESS);
			}
		}
		return epAttribute;
	}
}