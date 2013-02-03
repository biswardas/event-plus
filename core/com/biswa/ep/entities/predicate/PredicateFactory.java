package com.biswa.ep.entities.predicate;

import java.util.ArrayList;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.substance.DecimalSubstance;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;

public class PredicateFactory {
	private enum PredicateEnum {
		True(com.biswa.ep.entities.predicate.True.class), 
		False(com.biswa.ep.entities.predicate.False.class), 
		Eq(com.biswa.ep.entities.predicate.Eq.class), 
		Ne(com.biswa.ep.entities.predicate.Ne.class), 
		Lt(com.biswa.ep.entities.predicate.Lt.class), 
		Le(com.biswa.ep.entities.predicate.Le.class), 
		Ge(com.biswa.ep.entities.predicate.Ge.class), 
		Gt(com.biswa.ep.entities.predicate.Gt.class), 
		AEq(com.biswa.ep.entities.predicate.AEq.class), 
		ANe(com.biswa.ep.entities.predicate.ANe.class), 
		ALt(com.biswa.ep.entities.predicate.ALt.class), 
		ALe(com.biswa.ep.entities.predicate.ALe.class), 
		AGe(com.biswa.ep.entities.predicate.AGe.class), 
		AGt(com.biswa.ep.entities.predicate.AGt.class), 
		In(com.biswa.ep.entities.predicate.In.class), 
		And(com.biswa.ep.entities.predicate.And.class), 
		Or(com.biswa.ep.entities.predicate.Or.class);
		Class<? extends Predicate> predicateClass;

		PredicateEnum(Class<? extends Predicate> predicateClass) {
			this.predicateClass = predicateClass;
		}
	}

	private String name;
	private String attribute;
	private ArrayList<Substance> substanceList = new ArrayList<Substance>();
	private ArrayList<String> attributeList = new ArrayList<String>();

	public static PredicateFactory getFactory() {
		return new PredicateFactory();
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public void addDSubstance(String substance) {
		substanceList.add(new DecimalSubstance(Double.parseDouble(substance)));
	}

	public void addSSubstance(String substance) {
		substanceList.add(new ObjectSubstance(substance));
	}
	
	public void addAttributeOperand(String attribute) {
		attributeList.add(attribute);
	}

	public Predicate buildPredicate() {
		Attribute attr = null;
		Object obj[] = new Object[2];
		PredicateEnum predEnum = PredicateEnum.valueOf(name);
		Predicate p = null;
		try {
			switch (predEnum) {
			case And:
			case Or:
			case True:
			case False:
				p = (Predicate) predEnum.predicateClass.newInstance();
				break;
			case In:
				attr = new LeafAttribute(attribute);				
				obj[0] = attr;
				obj[1] = (Substance[])substanceList.toArray(new Substance[0]);
				p = (Predicate) predEnum.predicateClass.getConstructors()[0]
						.newInstance(obj);
				break;
			case Eq: 
			case Ne: 
			case Lt: 
			case Le: 
			case Ge: 
			case Gt:
				attr = new LeafAttribute(attribute);
				obj[0] = attr;
				obj[1] = substanceList.get(0);
				p = (Predicate) predEnum.predicateClass.getConstructors()[0]
						.newInstance(obj);
				break;
			case AEq: 
			case ANe: 
			case ALt: 
			case ALe: 
			case AGe: 
			case AGt:
				attr = new LeafAttribute(attribute);
				obj[0] = attr;
				obj[1] = new LeafAttribute((String)attributeList.get(0));
				p = (Predicate) predEnum.predicateClass.getConstructors()[0]
						.newInstance(obj);
				break;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return p;
	}
}
