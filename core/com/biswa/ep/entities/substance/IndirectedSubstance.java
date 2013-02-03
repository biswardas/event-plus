package com.biswa.ep.entities.substance;

public abstract class IndirectedSubstance extends AbstractSubstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6290684903688909571L;
	
	protected Substance actualSubstance;
	
	public IndirectedSubstance(Substance actualSubstance){
		this.actualSubstance = actualSubstance;
	}
	
	@Override
	public int compareTo(Substance o) {
		return actualSubstance.compareTo(o);
	}

	@Override
	public Object getValue() {
		return actualSubstance.getValue();
	}

	@Override
	public boolean isAggr() {
		return false;
	}
	
	@Override
	final public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actualSubstance.getValue() == null) ? 0 : actualSubstance.getValue().hashCode());
		return result;
	}
	@Override
	final public boolean equals(Object obj) {
		if (this.actualSubstance == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Substance))
			return false;
		final Substance other = (Substance) obj;
		if (actualSubstance.getValue() == null) {
			if (other.getValue() != null)
				return false;
		} else if (!actualSubstance.getValue().equals(other.getValue()))
			return false;
		return true;
	}
	
	@Override
	final public String toString() {
		return actualSubstance.getValue() == null ? "null" : actualSubstance.getValue().toString();
	}
}
