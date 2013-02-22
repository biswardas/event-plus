package com.biswa.ep.annotations;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedAnnotationTypes( {"com.biswa.ep.annotations.Generated" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class EPGeneratedProcessor extends AbstractProcessor {
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		for (Element element : roundEnv.getRootElements()) {
			try {
				FileObject fob = processingEnv.getFiler().createResource(
						StandardLocation.SOURCE_OUTPUT, "",
						element.getSimpleName()+".xml", element);
				Writer writer = fob.openWriter();
				element.accept(new GenSourceVisitor(writer),
						new HashMap<Object, Object>());
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return true;
	}
}
