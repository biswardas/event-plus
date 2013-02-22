package com.biswa.ep.annotations;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

@SupportedAnnotationTypes( { "com.biswa.ep.annotations.EPContext" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class EPAnnotationProcessor extends AbstractProcessor {
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		for (Element element : roundEnv.getRootElements()) {
			if (element.getAnnotation(Generated.class) == null) {
				System.out.println("Invoking on:" + element.getSimpleName());
				Trees trees = Trees.instance(processingEnv);
				TreePath tp = trees.getPath(element);
				try {
					CompilationUnitTree cu = tp.getCompilationUnit();
					visitAndGenerate(trees, cu, element);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				System.out.println("Processing Completed on:"
						+ element.getSimpleName());
			}
		}
		return false;
	}

	private void visitAndGenerate(Trees trees, CompilationUnitTree tree,
			Element element) throws IOException {
		String targetPackage = getTargetPackage(tree).toString();
		FileObject fob = processingEnv.getFiler().createSourceFile(targetPackage+"."+ element.getSimpleName());
		Writer writer = fob.openWriter();
		SourceTreeVisitor stv = new SourceTreeVisitor(writer,targetPackage, trees);
		tree.accept(stv, element);
		writer.close();
	}

	private CharSequence getTargetPackage(CompilationUnitTree tree) {
		return tree.getPackageName() != null ? "epimpl."+tree.getPackageName() :"epimpl";
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element,
			AnnotationMirror annotation, ExecutableElement member,
			String userText) {
		System.out.println("Element:" + element);
		System.out.println("AnnotationMirror:" + annotation);
		System.out.println("ExecutableElement:" + member);
		System.out.println("UserText:" + userText);
		return super.getCompletions(element, annotation, member, userText);
	}
}
