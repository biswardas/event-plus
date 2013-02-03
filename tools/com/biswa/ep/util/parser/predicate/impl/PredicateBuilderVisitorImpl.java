package com.biswa.ep.util.parser.predicate.impl;

import java.util.Stack;

import com.biswa.ep.entities.predicate.Predicate;
import com.biswa.ep.entities.predicate.PredicateFactory;
import com.biswa.ep.util.parser.predicate.*;


public class PredicateBuilderVisitorImpl implements PredicateBuilderVisitor{
	Stack<Predicate> globalPredicateStack = new Stack<Predicate>();
	PredicateFactory globalFactory = null; 
	
	@Override
	public Object visit(SimpleNode node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTStart node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTOrPredicate node, Object data) {
		PredicateFactory localFactory = PredicateFactory.getFactory();
		localFactory.setName("Or");
		globalPredicateStack.push(localFactory.buildPredicate());
		data = node.childrenAccept(this, data);
		Predicate localPredicate = globalPredicateStack.pop();
		chainOrRoot(localPredicate);		
		return data;
	}

	@Override
	public Object visit(ASTAndPredicate node, Object data) {
		PredicateFactory localFactory = PredicateFactory.getFactory();
		localFactory.setName("And");
		globalPredicateStack.push(localFactory.buildPredicate());
		data = node.childrenAccept(this, data);
		Predicate localPredicate = globalPredicateStack.pop();
		chainOrRoot(localPredicate);		
		return data;
	}

	@Override
	public Object visit(ASTSimplePredicate node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTAttribute node, Object data) {
		globalFactory.setAttribute(node.getImage());
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTAttributeOperand node, Object data) {
		globalFactory.addAttributeOperand(node.getImage());
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTOperand node, Object data) {
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTEndPoint node, Object data) {
		globalFactory = PredicateFactory.getFactory();
		globalFactory.setName(node.getImage());
		data = node.childrenAccept(this, data);
		Predicate localPredicate = globalFactory.buildPredicate();		
		globalFactory = null;
		chainOrRoot(localPredicate);
		return data;
	}


	@Override
	public Object visit(ASTTruePredicate node, Object data) {
		PredicateFactory localFactory = PredicateFactory.getFactory();
		localFactory.setName("True");
		globalPredicateStack.push(localFactory.buildPredicate());
		data = node.childrenAccept(this, data);
		Predicate localPredicate = globalPredicateStack.pop();
		chainOrRoot(localPredicate);		
		return data;
	}

	@Override
	public Object visit(ASTEmptyPredicate node, Object data) {
		PredicateFactory localFactory = PredicateFactory.getFactory();
		localFactory.setName("True");
		globalPredicateStack.push(localFactory.buildPredicate());
		data = node.childrenAccept(this, data);
		Predicate localPredicate = globalPredicateStack.pop();
		chainOrRoot(localPredicate);		
		return data;
	}

	@Override
	public Object visit(ASTFalsePredicate node, Object data) {
		PredicateFactory localFactory = PredicateFactory.getFactory();
		localFactory.setName("False");
		globalPredicateStack.push(localFactory.buildPredicate());
		data = node.childrenAccept(this, data);
		Predicate localPredicate = globalPredicateStack.pop();
		chainOrRoot(localPredicate);		
		return data;
	}

	private void chainOrRoot(Predicate localPredicate) {
		if(globalPredicateStack.isEmpty()){
			globalPredicateStack.push(localPredicate);
		}else{
			Predicate peekedPredicate = globalPredicateStack.peek();
			peekedPredicate.chain(localPredicate);
		}
	}

	@Override
	public Object visit(ASTSOperand node, Object data) {
		globalFactory.addSSubstance(node.getImage());
		return node.childrenAccept(this, data);
	}

	@Override
	public Object visit(ASTDOperand node, Object data) {
		globalFactory.addDSubstance(node.getImage());
		return node.childrenAccept(this, data);
	}
	public Predicate getPredicate(){
		return globalPredicateStack.peek();
	}

}
