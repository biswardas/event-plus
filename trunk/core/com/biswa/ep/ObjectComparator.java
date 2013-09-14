package com.biswa.ep;

import java.util.Comparator;
/**Utility class used to compare objects.Tries to handle lot of scenarios
 * may not be appropriate in all scenarios but good foe general purpose use.
 * 
 * @author Biswa
 *
 */
public class ObjectComparator implements Comparator<Object>{
	@SuppressWarnings("unchecked")
	@Override
	public int compare(Object o1Substance, Object o2Substance) {

		int compareValue = 0;
		if (o1Substance != null && o2Substance != null) {
			if (Number.class.isAssignableFrom(o1Substance.getClass())
					&& Number.class.isAssignableFrom(o2Substance.getClass())) {
				double d1 = ((Number) o1Substance).doubleValue();
				double d2 = ((Number) o2Substance).doubleValue();
				if(d1>d2){
					compareValue=1;
				}else if(d2>d1){
					compareValue=-1;
				}else{
					compareValue=0;
				}
			}else if (Comparable.class.isAssignableFrom(o1Substance.getClass())
					&& Comparable.class.isAssignableFrom(o2Substance.getClass())
					&& o1Substance.getClass().isAssignableFrom(o2Substance.getClass())) {		
				Comparable<Object> c1= (Comparable<Object>) o1Substance;	
				Comparable<Object> c2= (Comparable<Object>) o2Substance;
				compareValue = (c1.compareTo(c2));
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