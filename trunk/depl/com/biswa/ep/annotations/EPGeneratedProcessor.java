package com.biswa.ep.annotations;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.Generated;
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

@SupportedAnnotationTypes( {"javax.annotation.Generated" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class EPGeneratedProcessor extends AbstractProcessor {
	private EPContainerManager containerManager = new EPContainerManager();
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		//Pass 1 register all containers
		for (Element element : roundEnv.getRootElements()) {
			if(element.getAnnotation(Generated.class)!=null && element.getAnnotation(EPContext.class)!=null){
				for (Element innerElement : element.getEnclosedElements()) {
					EPContainer containerAnnot = innerElement
							.getAnnotation(EPContainer.class);
					if (containerAnnot != null) {
						TypeElement epContainer = (TypeElement) innerElement;
						containerManager.registerContainer(epContainer);
					}
				}
			}
		}

		//Pass 2 Generate Deployment descripters
		for (Element element : roundEnv.getRootElements()) {
			if(element.getAnnotation(Generated.class)!=null && element.getAnnotation(EPContext.class)!=null){
				try {
					FileObject fob = processingEnv.getFiler().createResource(
							StandardLocation.SOURCE_OUTPUT, "",
							element.getSimpleName()+".xml", element);
					Writer writer = fob.openWriter();
					element.accept(new GenSourceVisitor(containerManager,writer),null);
					writer.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return false;
	}

}
