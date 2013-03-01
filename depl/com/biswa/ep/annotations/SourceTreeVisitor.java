package com.biswa.ep.annotations;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.biswa.ep.annotations.EPCompilationException.ErrorCode;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.Trees;

/**
 * More Powerful
 * 
 * @author dasbib
 * 
 */
public class SourceTreeVisitor extends SimpleTreeVisitor<Boolean, Element> {
	private String currentContainerName;

	private class DependencyManager {
		private class TypeManager {
			private HashMap<String, HashMap<String, Element>> typeMap = new HashMap<String, HashMap<String, Element>>();
			private HashMap<String, HashSet<String>> inheritanceTree = new HashMap<String, HashSet<String>>();

			private Element lookUpType(String sourceClassName, String variable) {
				HashMap<String, Element> myMap = typeMap.get(sourceClassName);
				if (myMap == null) {
					throw new RuntimeException(sourceClassName
							+ " Is this a container? Detected dependency: "
							+ variable);
				}
				Element element = myMap.get(variable);
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

			public void add(String currentContainerName, String superClassName) {
				if (inheritanceTree.containsKey(currentContainerName)) {
					inheritanceTree.get(currentContainerName).add(
							superClassName);
				} else {
					HashSet<String> hs = new HashSet<String>();
					hs.add(superClassName);
					inheritanceTree.put(currentContainerName, hs);
				}
			}

			public void add(String currentContainerName, String memberName,
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

		private boolean collectDependencies = false;
		private final TypeManager typeManager = new TypeManager();
		private Set<String> dependencySet = new HashSet<String>();

		public boolean isInjectDependency() {
			return collectDependencies;
		}

		public void begin() {
			collectDependencies = true;
		}

		public void add(String attribute) {
			if (typeManager.lookUpType(currentContainerName, attribute) != null) {
				dependencySet.add(attribute);
			}
		}

		public void addInheritance(String className, String superClassName) {
			typeManager.add(className, superClassName);
		}

		public void addType(String className, String memberName, Element element) {
			typeManager.add(className, memberName, element);
		}

		public void reset() {
			collectDependencies = false;
			dependencySet.clear();
		}

		public Set<String> getDependency() {
			return dependencySet;
		}

		public boolean hasDependency() {
			return !dependencySet.isEmpty();
		}

		public String lookUpType(String oneDependency) {
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

		public EPAttribute getEPAttribute(String memberName) {			
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

	private final DependencyManager dependencyManager = new DependencyManager();
	private Writer writer;
	private Trees trees;
	private String targetPackage;

	public SourceTreeVisitor(Writer writer, String targetPackage, Trees trees) {
		this.writer = writer;
		this.trees = trees;
		this.targetPackage = targetPackage;
	}

	public SourceTreeVisitor(Trees trees) {
		this(new OutputStreamWriter(System.out), null, trees);
	}

	@Override
	protected Boolean defaultAction(Tree tree, Element e) {
		write(tree.toString());
		return true;
	}

	@Override
	public Boolean visitIdentifier(IdentifierTree arg0, Element arg1) {
		boolean returnValue = true;
		if (dependencyManager.isInjectDependency()) {
			dependencyManager.add(arg0.getName().toString());
		}
		return returnValue;
	}

	@Override
	public Boolean visitBinary(BinaryTree arg0, Element arg1) {
		boolean returnValue = true;
		if (dependencyManager.isInjectDependency()) {
			arg0.getLeftOperand().accept(this, arg1);
			arg0.getRightOperand().accept(this, arg1);
		}
		return returnValue;
	}

	@Override
	public Boolean visitParenthesized(ParenthesizedTree arg0, Element arg1) {
		boolean returnValue = true;
		if (dependencyManager.isInjectDependency()) {
			returnValue = arg0.getExpression().accept(this, arg1);
		}
		return returnValue;
	}

	@Override
	public Boolean visitNewArray(NewArrayTree arg0, Element arg1) {
		boolean returnValue = true;
		if (dependencyManager.isInjectDependency()) {
			if (arg0.getInitializers() != null) {
				for (ExpressionTree initTree : arg0.getInitializers()) {
					initTree.accept(this, arg1);
				}
			}
			if (arg0.getDimensions() != null) {
				for (ExpressionTree dimTree : arg0.getDimensions()) {
					dimTree.accept(this, arg1);
				}
			}

		}
		return returnValue;
	}

	@Override
	public Boolean visitMethodInvocation(MethodInvocationTree arg0, Element arg1) {
		boolean returnValue = true;
		if (dependencyManager.isInjectDependency()) {
			ExpressionTree methodbase = arg0.getMethodSelect();
			methodbase.accept(this, arg1);
			List<? extends ExpressionTree> arguments = arg0.getArguments();
			for (Tree oneArgument : arguments) {
				oneArgument.accept(this, arg1);
			}
		} else {
			super.visitMethodInvocation(arg0, arg1);
		}
		return returnValue;
	}

	@Override
	public Boolean visitConditionalExpression(ConditionalExpressionTree arg0,
			Element arg1) {
		boolean returnValue = true;
		if (dependencyManager.isInjectDependency()) {
			arg0.getCondition().accept(this, arg1);
			arg0.getTrueExpression().accept(this, arg1);
			arg0.getFalseExpression().accept(this, arg1);
		} else {
			returnValue = super.visitConditionalExpression(arg0, arg1);
		}
		return returnValue;
	}

	@Override
	public Boolean visitTypeCast(TypeCastTree arg0, Element arg1) {
		boolean returnValue = true;
		if (dependencyManager.isInjectDependency()) {
			arg0.getExpression().accept(this, arg1);
		} else {
			returnValue = super.visitTypeCast(arg0, arg1);
		}
		return returnValue;
	}

	@Override
	public Boolean visitLiteral(LiteralTree arg0, Element arg1) {
		return true;
	}

	@Override
	public Boolean visitArrayAccess(ArrayAccessTree arg0, Element arg1) {
		boolean returnValue = true;
		if (dependencyManager.isInjectDependency()) {
			arg0.getExpression().accept(this, arg1);
			arg0.getIndex().accept(this, arg1);
		} else {
			returnValue = super.visitArrayAccess(arg0, arg1);
		}
		return returnValue;
	}

	@Override
	public Boolean visitInstanceOf(InstanceOfTree arg0, Element arg1) {
		boolean returnValue = true;
		if (dependencyManager.isInjectDependency()) {
			arg0.getExpression().accept(this, arg1);
		} else {
			returnValue = super.visitInstanceOf(arg0, arg1);
		}
		return returnValue;
	}

	@Override
	public Boolean visitNewClass(NewClassTree arg0, Element arg1) {
		boolean returnValue = true;
		if (dependencyManager.isInjectDependency()) {
			for (ExpressionTree exprTree : arg0.getArguments()) {
				exprTree.accept(this, arg1);
			}
		} else {
			returnValue = super.visitNewClass(arg0, arg1);
		}
		return returnValue;
	}

	@Override
	public Boolean visitUnary(UnaryTree arg0, Element arg1) {
		boolean returnValue = true;
		if (dependencyManager.isInjectDependency()) {
			arg0.getExpression().accept(this, arg1);
		} else {
			returnValue = super.visitUnary(arg0, arg1);
		}
		return returnValue;
	}

	@Override
	public Boolean visitMemberSelect(MemberSelectTree arg0, Element arg1) {
		boolean returnValue = true;
		// TODO revisit
		if (dependencyManager.isInjectDependency()) {
			arg0.getExpression().accept(this, arg1);
			dependencyManager.add(arg0.getIdentifier().toString());
		} else {
			returnValue = super.visitMemberSelect(arg0, arg1);
		}
		return returnValue;
	}

	@Override
	public Boolean visitCompilationUnit(CompilationUnitTree arg0, Element arg1) {
		for (Tree smallTree : arg0.getPackageAnnotations()) {
			smallTree.accept(this, arg1);
		}
		if (arg0.getPackageName() != null) {
			writeln("package " + targetPackage + ";");
		}
		writeln("import java.util.concurrent.*;");
		writeln("import java.util.*;");
		writeln("import javax.annotation.*;");
		writeln("import com.biswa.ep.annotations.*;");
		writeln("import com.biswa.ep.entities.transaction.*;");		
		writeln("import com.biswa.ep.entities.*;");
		writeln("import com.biswa.ep.entities.substance.*;");
		EPContext epContext = arg1.getAnnotation(EPContext.class);
		for (String oneImport : epContext.packages()) {
			writeln("import " + oneImport + ";");
		}
		for (String oneImport : epContext.schemas()) {
			writeln("import " + targetPackage + "." + oneImport + ".*;");
		}
		for (Tree smallTree : arg0.getImports()) {
			smallTree.accept(this, arg1);
		}
		// for (Element element : arg1.getEnclosedElements()) {
		// if (element.getAnnotation(Generated.class) == null) {
		Tree smallTree = trees.getTree(arg1);
		smallTree.accept(this, arg1);
		// }
		// }
		return true;
	}

	@Override
	public Boolean visitClass(ClassTree arg0, Element arg1) {
		boolean returnValue = true;
		if (arg1.getAnnotation(EPContext.class) != null) {
			generateEPContext(arg0, arg1);
		} else if (arg1.getAnnotation(EPContainer.class) != null) {
			generateEPContainer(arg0, arg1);
		} else {
			returnValue = super.visitClass(arg0, arg1);
		}
		return returnValue;
	}

	private void generateEPContainer(ClassTree arg0, Element arg1) {
		TypeElement typeElement = (TypeElement) arg1;
		currentContainerName = typeElement.getQualifiedName().toString();
		if (arg1.getKind() == ElementKind.CLASS) {
			write(arg0.getModifiers().toString());
			write("class");
			write(" " + arg0.getSimpleName());
			if (arg0.getExtendsClause() != null) {
				if (isEPContainer(((DeclaredType) typeElement.getSuperclass())
						.asElement())) {
					dependencyManager.addInheritance(currentContainerName,
							typeElement.getSuperclass().toString());
				}
				write(" extends "
						+ ((DeclaredType) typeElement.getSuperclass())
								.asElement().getSimpleName() + " ");
			}
			if (arg0.getImplementsClause().size() > 0) {
				write(" implements ");
				applyExtends(typeElement);
			}
		} else {
			write(arg0.getModifiers().toString());
			write(" " + arg0.getSimpleName());
			if (arg0.getImplementsClause().size() > 0) {
				write(" extends ");
				applyExtends(typeElement);
			}
		}
		writeln("{");
		for (Element containerElement : arg1.getEnclosedElements()) {
			Tree containerTree = trees.getTree(containerElement);
			containerTree.accept(this, containerElement);
		}

		EPContainer epContainerAnn = arg1.getAnnotation(EPContainer.class);
		if (!epContainerAnn.generator().isEmpty()) {
			generateInlet(epContainerAnn);
		}
		writeln("}");
	}

	private void generateInlet(EPContainer epContainerAnn) {
		writeln("static public class Inlet extends SimpleInlet{");
		writeln("private " +epContainerAnn.generator() + " generator =new "
				+ epContainerAnn.generator() + "();");
		writeln("@Override");
		writeln("protected void failSafeInit() throws Exception{generator.init(queue);}");
		writeln("}");
	}

	private void generateEPContext(ClassTree arg0, Element arg1) {
		writeln("@Generated(date=\"" + new java.util.Date().toString()
				+ "\",comments=\""+getVersion()+"\",value={\"" + System.getProperty("user.name") + "\"})");
		write(arg0.getModifiers() + " " + arg0.getSimpleName());
		if (arg0.getExtendsClause() != null) {
			arg0.getExtendsClause().accept(this, arg1);
		}
		if (arg0.getImplementsClause().size() > 0) {
			write(" extends ");
			for (Tree smallTree : arg0.getImplementsClause()) {
				write(smallTree.toString());
			}
		}
		writeln("{");
		for (Element containerElement : arg1.getEnclosedElements()) {
			Tree containerTree = trees.getTree(containerElement);
			containerTree.accept(this, containerElement);
		}
		writeln("}");
	}

	private void applyExtends(TypeElement typeElement) {
		List<? extends TypeMirror> interfaceList = typeElement.getInterfaces();
		Iterator<? extends TypeMirror> iter = interfaceList.iterator();
		while (iter.hasNext()) {
			TypeMirror oneInterface = iter.next();
			Element asElement = ((DeclaredType) oneInterface).asElement();
			write(asElement.getSimpleName().toString());

			if (isEPContainer(asElement)) {
				dependencyManager.addInheritance(currentContainerName,
						oneInterface.toString());
			}
			if (iter.hasNext()) {
				write(",");
			}
		}
	}

	@Override
	public Boolean visitVariable(VariableTree arg0, Element arg1) {
		dependencyManager.addType(currentContainerName, arg0.getName()
				.toString(), arg1);
		boolean returnValue = true;
		Tree initializer = arg0.getInitializer();
		if (initializer != null) {
			dependencyManager.begin();
			initializer.accept(this, arg1);
		}
		beginClass(arg0.getName(), arg0.getModifiers().getAnnotations(), arg1);
		if (arg0.getInitializer() != null) {
			writeln("" + arg0.getType() + " " + arg0.getName() + "="
					+ arg0.getInitializer() + ";");
		} else {
			writeln("" + arg0.getType() + " " + arg0.getName() + ";");
		}

		writeln("return new ObjectSubstance(" + arg0.getName() + ");");

		writeln("}");

		writeln("}");
		return returnValue;
	}

	@Override
	public Boolean visitMethod(MethodTree arg0, Element arg1) {
		boolean returnValue = true;
		if (arg1.getKind() != ElementKind.CONSTRUCTOR) {
			dependencyManager.addType(currentContainerName, arg0.getName()
					.toString(), arg1);
			if (arg0.getParameters().size() > 0) {
				dependencyManager.begin();
				for (VariableTree tree : arg0.getParameters()) {
					dependencyManager.add(tree.getName().toString());
				}
			}
			beginClass(arg0.getName(), arg0.getModifiers().getAnnotations(),
					arg1);
			if (!arg0.getReturnType().toString().equals("void")) {
				write(arg0.getReturnType() + " " + arg0.getName() + " = "
						+ arg0.getName() + "(");
			} else {
				write(arg0.getName() + "(");
			}
			Iterator<? extends VariableTree> iter = arg0.getParameters()
					.iterator();
			while (iter.hasNext()) {
				write(iter.next().getName().toString());
				if (iter.hasNext()) {
					write(",");
				}
			}
			writeln(");");
			if (!arg0.getReturnType().toString().equals("void")) {
				writeln("return new ObjectSubstance(" + arg0.getName() + ");");
			} else {
				writeln("return InvalidSubstance.INVALID_SUBSTANCE;");
			}

			writeln("}");

			returnValue = super.visitMethod(arg0, arg1);

			writeln("}");
		}
		return returnValue;
	}

	private void beginClass(Name name, List<? extends AnnotationTree> list,
			Element arg1) {
		EPContainer epContainer = arg1.getEnclosingElement().getAnnotation(
				EPContainer.class);
		// Insert Annotations
		for (AnnotationTree at : list) {
			at.accept(this, arg1);
		}
		// Generate Class
		EPAttribute epAttribute = null;
		if ((epAttribute = arg1.getAnnotation(EPAttribute.class)) == null) {
			if(arg1.getModifiers().contains(Modifier.PRIVATE)){
				writeln("@EPAttribute(type = EPAttrType.Private)");
				writeln("public static class " + name + " extends PrivateAttribute{");
			}else{
				writeln("@EPAttribute(type = EPAttrType.Member)");
				writeln("public static class " + name + " extends Attribute{");
			}
		} else {
			writeln("public static class " + name + " extends "
					+ epAttribute.type().getName() + "{");		
			if(arg1.getModifiers().contains(Modifier.PRIVATE)){
				writeln("@Override public boolean propagate(){return false;}");
			}
		}
		writeln("private static final long serialVersionUID = 1L;");
		EPAttrType attrType = getEPAttrType(epAttribute, epContainer);
		if (dependencyManager.hasDependency()) {
			writeln("{");

			for (String oneDependency : dependencyManager.getDependency()) {
				if (checkDependency(epAttribute,
						dependencyManager.getEPAttribute(oneDependency))) {
					writeln("addDependency(new " + oneDependency
							+ "());");
				} else {
					throw new RuntimeException(
							"Non Permissible dependency encountered. Member "
									+ name + " can not depend on "
									+ oneDependency);
				}
			}

			writeln("}");
		}
		// Generate Constructor
		writeln("public " + name + "(){");
		writeln("super(\"" + name + "\");");
		writeln("}");

		switch (attrType) {
		case SubProcessor:
			writeln("private " +epAttribute.processor() + " processor =new "
					+ epAttribute.processor() + "();");

			// Generate Eval Function
			writeln("@Override");
			writeln("protected void failSafeInit() throws Exception{processor.init(queue);}");
			writeln("@Override");
			writeln("public Object subscribe(Object object){"
					+ "return processor.subscribe(object);" + "}");
			writeln("@Override");
			writeln("public void unsubscribe(Object object){"
					+ "processor.unsubscribe(object);" + "}");
			writeln("public Object dontcare(Object object){");
			break;
		case Static:
			// Generate Eval Function
			writeln("@Override");
			writeln("public Substance evaluate(Attribute att){");
			for (String oneDependency : dependencyManager.getDependency()) {
				String type = dependencyManager.lookUpType(oneDependency);
				writeln(type + " " + oneDependency + " =(" + type
						+ ") super.getStatic(\"" + oneDependency + "" + "\");");
			}
			break;
		default:
			// Generate Eval Function
			writeln("@Override");
			writeln("public Substance evaluate(Attribute att,ContainerEntry ce){");
			for (String oneDependency : dependencyManager.getDependency()) {
				String type = dependencyManager.lookUpType(oneDependency);
				EPAttribute dependee = dependencyManager.getEPAttribute(oneDependency);
				if(dependee!=null && dependee.type()==EPAttrType.Static){
					writeln(type + " " + oneDependency + " =(" + type
							+ ") super.getStatic(\"" + oneDependency + "" + "\");");
				}else{
					writeln(type + " " + oneDependency + " =(" + type
							+ ") super.getValue(ce,\"" + oneDependency + ""
							+ "\");");					
				}
			}
		}
		dependencyManager.reset();
	}

	private EPAttrType getEPAttrType(EPAttribute epAttribute,
			EPContainer epContainer) {
		if (epContainer.type() == EPConType.Static) {
			return EPAttrType.Static;
		}
		EPAttrType attrType = EPAttrType.Member;
		if (epAttribute != null) {
			attrType = epAttribute.type();
		}
		return attrType;
	}

	private boolean checkDependency(EPAttribute dependant, EPAttribute dependee) {
		int dependentValue = dependant != null ? dependant.type()
				.depedencyValue() : EPAttrType.Member.depedencyValue();
		int dependeeValue = dependee != null ? dependee.type().depedencyValue()
				: EPAttrType.Member.depedencyValue();
		return dependentValue >= dependeeValue;
	}

	private void writeln(String str) {
		write(str);
		write("\n");
	}

	private void write(String str) {
		try {
			writer.write(str);
			writer.flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isEPContainer(Element e) {
		return e.getAnnotation(EPContainer.class) != null;
	}

	private static String getVersion(){
		Class<EPContext> klass = EPContext.class;
		URL location = klass.getResource('/'
				+ klass.getName().replace('.', '/') + ".class");
		JarURLConnection uc;
		String version = "";
		try {
			uc = (JarURLConnection) location.openConnection();
			Attributes attr = uc.getMainAttributes();
			version = (attr != null ? attr
					.getValue(Attributes.Name.IMPLEMENTATION_VERSION) : null);
		} catch (Exception e) {
			version="0.0";
		}
		return version;
	}
}