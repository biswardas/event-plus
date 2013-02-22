/**
 * 
 */
package com.biswa.ep.annotations;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.PrivateAttribute;
import com.biswa.ep.entities.StatelessAttribute;
import com.biswa.ep.entities.StaticAttribute;
import com.biswa.ep.subscription.SimpleSubscriptionProcessor;

public enum EPAttrType {
	Static(StaticAttribute.class, 0), Private(PrivateAttribute.class, 1), Member(
			Attribute.class, 1), Subscriber(Attribute.class,
			1), SubProcessor(SimpleSubscriptionProcessor.class,2),Stateless(StatelessAttribute.class, 3), ;
	private String name = Attribute.class.getName();
	private int dependency;

	EPAttrType(Class<? extends Attribute> className, int dependency) {
		this.name = className.getName();
		this.dependency = dependency;
	}

	public String getName() {
		return name;
	}

	public int depedencyValue() {
		return dependency;
	}
}