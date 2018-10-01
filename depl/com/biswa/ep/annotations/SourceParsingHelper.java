package com.biswa.ep.annotations;

import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

public interface SourceParsingHelper {
	public default String extractProcessor(Element arg1) {
		return className(arg1,EPAttribute.class.getName(),"processor");
	}
	public default String extractGenerator(Element arg1) {
		return className(arg1,EPContainer.class.getName(),"generator");
	}
	public default String className(Element arg1,String className,String annotMemName) {
		for(AnnotationMirror oneMirror:arg1.getAnnotationMirrors()){
			if(String.valueOf(oneMirror.getAnnotationType()).equals(className)){
				for(Entry<? extends ExecutableElement, ? extends AnnotationValue> oneItem:oneMirror.getElementValues().entrySet()){
					if(oneItem.getKey().getSimpleName().toString().equals(annotMemName)){
						return String.valueOf(oneItem.getValue().getValue());
					}
				}
			}
		}
		return null;
	}
}
