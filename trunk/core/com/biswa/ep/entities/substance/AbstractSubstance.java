package com.biswa.ep.entities.substance;

/**Abstract substance contains the common behavior for the substances. 
 * 
 * @author biswa
 *
 */

abstract public class AbstractSubstance implements Substance{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2263417413076655421L;
	
	@Override
	public boolean isAggr() {
		return false;
	}
	
	@Override
	public boolean isMultiValue(){
		return false;
	}
	
	@Override
	public int compareTo(Substance incomingSubstance){
		if(getValue()!=null && getValue() instanceof Number){
			Double number = new Double(getValue().toString());
			if(incomingSubstance.getValue()!=null && incomingSubstance.getValue() instanceof Number){
				Double secNumber = new Double(incomingSubstance.getValue().toString());
				return number.compareTo(secNumber);
			}			
		}
		return getValue().toString().compareTo(incomingSubstance.getValue().toString());
	}
	@Override
	public String toString() {
		return getValue() == null ? "null" : getValue().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Substance))
			return false;
		final Substance other = (Substance) obj;
		if (getValue() == null) {
			if (other.getValue() != null)
				return false;
		} else if (!getValue().equals(other.getValue()))
			return false;
		return true;
	}
}
