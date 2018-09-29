package com.biswa.ep.annotations;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.logging.Logger;

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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

@SupportedAnnotationTypes( { "com.biswa.ep.annotations.EPContext" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class EPAnnotationProcessor extends AbstractProcessor {
	private static final Logger logger = Logger.getLogger(EPAnnotationProcessor.class.getName());
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
			if(element.getAnnotation(Generated.class) == null && element.getAnnotation(EPContext.class)!=null){
				logger.info("Pass 1 Registering Container:" + element.getSimpleName());
				containerManager.registerSchema((TypeElement)element);
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

		//Pass 2 generate implementation sources
		for (Element element : roundEnv.getRootElements()) {
			if (element.getAnnotation(Generated.class) == null && element.getAnnotation(EPContext.class)!=null) {
				logger.info("Pass 2 Translating:" + element.getSimpleName());
				Trees trees = Trees.instance(processingEnv);
				TreePath tp = trees.getPath(element);
				try {
					CompilationUnitTree cu = tp.getCompilationUnit();
					visitAndGenerate(trees, cu, element);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return false;
	}

	private void visitAndGenerate(Trees trees, CompilationUnitTree tree,
			Element element) throws IOException {
		String targetPackage = getTargetPackage(tree).toString();
		FileObject fob = processingEnv.getFiler().createSourceFile(targetPackage+"."+ element.getSimpleName());
		Writer writer = fob.openWriter();
		SourceTreeVisitor stv = new SourceTreeVisitor(containerManager,writer,targetPackage, trees);
		tree.accept(stv, element);
		writer.close();
	}

	private CharSequence getTargetPackage(CompilationUnitTree tree) {
		return tree.getPackageName() != null ? "epimpl."+tree.getPackageName() :"epimpl";
	}
}
