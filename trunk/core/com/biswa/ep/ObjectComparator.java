package com.biswa.ep;

import java.util.Comparator;

public class ObjectComparator implements Comparator<Object>{
	@Override
	public int compare(Object o1Substance, Object o2Substance) {

		int compareValue = 0;
		if (o1Substance != null && o2Substance != null) {
			if (o1Substance.getClass().isAssignableFrom(
					Number.class)
					&& o2Substance.getClass().isAssignableFrom(
							Number.class)) {
				compareValue = compare((Number) o1Substance,
						(Number) o2Substance);
			}else{
				compareValue = o1Substance.toString().compareTo(o2Substance.toString());
			}
		} else if (o1Substance == null && o2Substance != null) {
			compareValue = -1;
		} else if (o1Substance == null && o2Substance != null) {
			compareValue = 1;
		}
		return compareValue;
	}	
}