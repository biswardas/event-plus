package com.biswa.ep.provider;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.tools.SimpleJavaFileObject;

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
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;

public class EPJavaObject extends SimpleJavaFileObject {
	private String expression;

	protected EPJavaObject(String expression) {
		super(URI.create("string:///EPExpression.java"), Kind.SOURCE);
		this.expression = expression;
		JavaCompiler jc = new JavaCompiler(new Context());
		CompilationUnitTree cuTree = jc.parse(this);
		if (jc.errorCount() > 0) {
			throw new RuntimeException("Failed compiling expression..");
		}
		cuTree.getTypeDecls().get(0).accept(new TreeVisitor(), null);
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors)
			throws IOException {
		return "class EPExpression{Object " + expression + ";}";
	}

	public Collection<String> getVariables() {
		return al;
	}

	public static void main(String[] args) {
		EPJavaObject jfo = new EPJavaObject("x=a+b");
		System.out.println(jfo.getVariables());
	}

	private Set<String> al = new HashSet<String>();

	class TreeVisitor extends SimpleTreeVisitor<Boolean, Element> {
		@Override
		protected Boolean defaultAction(Tree tree, Element e) {
			System.err.println(tree);
			return true;
		}

		@Override
		public Boolean visitIdentifier(IdentifierTree arg0, Element arg1) {
			boolean returnValue = true;
			al.add(arg0.getName().toString());
			return returnValue;
		}

		@Override
		public Boolean visitBinary(BinaryTree arg0, Element arg1) {
			boolean returnValue = true;
			arg0.getLeftOperand().accept(this, arg1);
			arg0.getRightOperand().accept(this, arg1);
			return returnValue;
		}

		@Override
		public Boolean visitParenthesized(ParenthesizedTree arg0, Element arg1) {
			boolean returnValue = true;
			returnValue = arg0.getExpression().accept(this, arg1);
			return returnValue;
		}

		@Override
		public Boolean visitNewArray(NewArrayTree arg0, Element arg1) {
			boolean returnValue = true;
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
			return returnValue;
		}

		@Override
		public Boolean visitMethodInvocation(MethodInvocationTree arg0,
				Element arg1) {
			boolean returnValue = true;
			ExpressionTree methodbase = arg0.getMethodSelect();
			methodbase.accept(this, arg1);
			List<? extends ExpressionTree> arguments = arg0.getArguments();
			for (Tree oneArgument : arguments) {
				oneArgument.accept(this, arg1);
			}
			return returnValue;
		}

		@Override
		public Boolean visitConditionalExpression(
				ConditionalExpressionTree arg0, Element arg1) {
			boolean returnValue = true;
			arg0.getCondition().accept(this, arg1);
			arg0.getTrueExpression().accept(this, arg1);
			arg0.getFalseExpression().accept(this, arg1);
			return returnValue;
		}

		@Override
		public Boolean visitTypeCast(TypeCastTree arg0, Element arg1) {
			boolean returnValue = true;
			arg0.getExpression().accept(this, arg1);
			return returnValue;
		}

		@Override
		public Boolean visitLiteral(LiteralTree arg0, Element arg1) {
			return true;
		}

		@Override
		public Boolean visitArrayAccess(ArrayAccessTree arg0, Element arg1) {
			boolean returnValue = true;
			arg0.getExpression().accept(this, arg1);
			arg0.getIndex().accept(this, arg1);
			return returnValue;
		}

		@Override
		public Boolean visitInstanceOf(InstanceOfTree arg0, Element arg1) {
			boolean returnValue = true;
			arg0.getExpression().accept(this, arg1);
			return returnValue;
		}

		@Override
		public Boolean visitNewClass(NewClassTree arg0, Element arg1) {
			boolean returnValue = true;
			for (ExpressionTree exprTree : arg0.getArguments()) {
				exprTree.accept(this, arg1);
			}
			return returnValue;
		}

		@Override
		public Boolean visitUnary(UnaryTree arg0, Element arg1) {
			boolean returnValue = true;
			arg0.getExpression().accept(this, arg1);
			return returnValue;
		}

		@Override
		public Boolean visitMemberSelect(MemberSelectTree arg0, Element arg1) {
			boolean returnValue = true;
			arg0.getExpression().accept(this, arg1);
			return returnValue;
		}

		@Override
		public Boolean visitClass(ClassTree arg0, Element arg1) {
			boolean returnValue = true;
			arg0.getMembers().get(0).accept(this, arg1);
			return returnValue;
		}

		@Override
		public Boolean visitVariable(VariableTree arg0, Element arg1) {
			boolean returnValue = true;
			Tree initializer = arg0.getInitializer();
			initializer.accept(this, arg1);
			return returnValue;
		}
	}
}