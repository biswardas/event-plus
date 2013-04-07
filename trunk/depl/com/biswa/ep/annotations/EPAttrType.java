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
	Static(StaticAttribute.class) {

		@Override
		public boolean isDependencyAllowed(EPAttrType guest) {
			switch (guest) {
			case Static:
				return true;
			default:
				return false;
			}
		}

	},
	Private(PrivateAttribute.class) {

		@Override
		public boolean isDependencyAllowed(EPAttrType guest) {
			switch (guest) {
			case Stateless:
				return false;
			default:
				return true;
			}
		}

	},
	Member(Attribute.class) {

		@Override
		public boolean isDependencyAllowed(EPAttrType guest) {
			switch (guest) {
			case Stateless:
				return false;
			default:
				return true;
			}
		}

	},
	Subscriber(Attribute.class) {
		@Override
		public boolean isDependencyAllowed(EPAttrType guest) {
			return false;
		}

	},
	SubProcessor(SimpleSubscriptionProcessor.class) {
		@Override
		public boolean isDependencyAllowed(EPAttrType guest) {
			return false;
		}
	},
	Stateless(StatelessAttribute.class) {
		@Override
		public boolean isDependencyAllowed(EPAttrType guest) {
			return true;
		}
	};
	private String name;

	EPAttrType(Class<? extends Attribute> className) {
		this.name = className.getName();
	}

	public String getName() {
		return name;
	}

	public abstract boolean isDependencyAllowed(EPAttrType guest);
}