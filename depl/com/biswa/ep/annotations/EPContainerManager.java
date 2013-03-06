package com.biswa.ep.annotations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.biswa.ep.annotations.EPCompilationException.ErrorCode;


public class EPContainerManager {

	private HashMap<String, String> nameToQNameMap = new HashMap<String, String>();
	
	private HashMap<String, HashSet<String>> inheritanceTree = new HashMap<String, HashSet<String>>();

	private HashMap<String, TypeElement> containers = new HashMap<String, TypeElement>();

	public void registerContainer(TypeElement epContainer) {
		nameToQNameMap.put(epContainer.getSimpleName().toString(), epContainer.getQualifiedName().toString());
		containers.put(epContainer.getQualifiedName().toString(), epContainer);
		List<? extends TypeMirror> interfaceList = epContainer.getInterfaces();
		Iterator<? extends TypeMirror> iter = interfaceList.iterator();
		while (iter.hasNext()) {
			TypeMirror oneInterface = iter.next();
			Element asElement = ((DeclaredType) oneInterface).asElement();
			if (asElement.getAnnotation(EPContainer.class)!=null) {
				addInheritance(epContainer.getQualifiedName().toString(),
						oneInterface.toString());
			}
		}
		if(epContainer.getSuperclass().getKind()==TypeKind.DECLARED){
			TypeElement typeElement = (TypeElement) ((DeclaredType) epContainer.getSuperclass()).asElement();
			if (typeElement.getAnnotation(EPContainer.class)!=null) {
				addInheritance(epContainer.getQualifiedName().toString(),
						typeElement.getQualifiedName().toString());
			}
		}
	}

	public TypeElement getContainerBySimpleName(String name) {
		if(nameToQNameMap.containsKey(name)){
			return containers.get(nameToQNameMap.get(name));
		}else{
			throw new EPCompilationException("Unknown containerDont know about container");
		}
	}

	public boolean isProxy(String name) {
		TypeElement element = getContainerBySimpleName(name);
		return element.getAnnotation(EPContainer.class).type() == EPConType.Proxy;
	}
	
	public boolean supportsFeedback(String name) {
		TypeElement element = getContainerBySimpleName(name);
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

	public String typeKnown(String currentContainerName,String attribute) {
		Element element = lookUpElement(currentContainerName, attribute);
		if(element!=null){
			return element.getEnclosingElement().getSimpleName().toString();
		}else{
			return null;
		}
	}

	public void addInheritance(String className, String superClassName) {
		if (inheritanceTree.containsKey(className)) {
			inheritanceTree.get(className).add(
					superClassName);
		} else {
			HashSet<String> hs = new HashSet<String>();
			hs.add(superClassName);
			inheritanceTree.put(className, hs);
		}
	}
	
	public String lookUpType(String currentContainerName,String oneDependency) {
		String type = null;
		Element element = lookUpElement(currentContainerName, oneDependency);
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
		Element element = lookUpElement(currentContainerName, memberName);
		EPAttribute epAttribute=element.getAnnotation(EPAttribute.class);
		if(!currentContainerName.equals(element.getEnclosingElement().asType().toString())){
			if(element.getModifiers().contains(Modifier.PRIVATE)){
				throw new EPCompilationException("Access privilage violation in container :"+currentContainerName+" member:"+memberName,ErrorCode.ACCESS);
			}
		}
		return epAttribute;
	}

	private Element lookUpElement(String sourceClassName, String variable) {
		Element element = null;
		TypeElement typeElement = containers.get(sourceClassName);
		//Lookup in immediate container
		if (typeElement != null) {
			for(Element oneElement:typeElement.getEnclosedElements()){
				if(oneElement.getKind()==ElementKind.METHOD || oneElement.getKind()==ElementKind.FIELD){
					if(oneElement.getSimpleName().toString().equals(variable)){
						element = oneElement;
						break;
					}
				}
			}
		}

		//Lookup in referenced container
		if (typeElement != null) {
			for(EPRef oneRef:typeElement.getAnnotation(EPContainer.class).ref()){
				if((element = lookUpElement(nameToQNameMap.get(oneRef.container()), variable))!=null){
					break;
				}
				
			}
		}
		//Look up in inherited containers
		if (element == null) {
			Set<String> parentType = inheritanceTree
					.get(sourceClassName);
			if (parentType != null) {
				for (String oneSuper : parentType) {
					element = lookUpElement(oneSuper, variable);
					if (element != null) {
						break;
					}
				}
			}
		}
		return element;
	}
}