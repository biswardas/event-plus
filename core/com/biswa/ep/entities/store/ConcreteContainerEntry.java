package com.biswa.ep.entities.store;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.TransportEntry;
import com.biswa.ep.entities.substance.Substance;
/**Container Entry can be thought of one db record in a table.
 * 
 * @author biswa
 *
 */
class ConcreteContainerEntry extends AbstractPhysicalEntry{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8719027377801534629L;
	//Substance are kept here for this record with index as the attribute's ordinal.
	private Substance[] substanceArray;

	
	protected ConcreteContainerEntry() {
		super(0);
		ConcreteContainer concreteContainer = this.getContainer();
		substanceArray=new Substance[concreteContainer.getPhysicalSize()];
	}
	
	/** The actual entry which is made into the container. This creates a concrete entry
	 * 
	 * @param externalidentity
	 * @param cs
	 */
	protected ConcreteContainerEntry(int externalidentity) {
		super(externalidentity);
		ConcreteContainer concreteContainer = this.getContainer();
		substanceArray=((ConcreteContainerEntry)concreteContainer.getDefaultEntry()).substanceArray.clone();
	}





	@Override
	final public Substance getSubstance(Attribute attribute) {
		return substanceArray[attribute.getOrdinal()];
	}
	
	@Override
	public Substance silentUpdate(Attribute attribute, Substance substance){
		return substanceArray[attribute.getOrdinal()]= substance;
	}
	
	@Override
	public Substance silentUpdate(Attribute attribute, Substance substance,int minor){
		return silentUpdate(attribute,substance);
	}

	@Override
	public void remove(Attribute attribute){
		substanceArray[attribute.getOrdinal()]= null;
	}
	
	@Override
	public void remove(Attribute attribute,int minor){
		remove(attribute);
	}
	
	@Override
	public String toString() {
		return "ContainerEntry [externalIdentity=" + externalIdentity
				+ ", containerEntryStore=" + Arrays.asList(substanceArray)
				+ ", metainfo=" + metainfo + "]";
	}

	@Override
	final public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + externalIdentity;
		return result;
	}

	@Override
	final public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		ContainerEntry other = (ContainerEntry) obj;
		if (externalIdentity != other.getIdentitySequence())
			return false;
		return true;
	}
		
	@Override
	public TransportEntry cloneConcrete(){
		Map<Attribute,Substance> entryQualifier=null;
		if(getContainer()!=null){
			entryQualifier = new HashMap<Attribute,Substance>();
			for(Attribute attribute:getContainer().getSubscribedAttributes()){
				if(attribute.propagate() && attribute.requiresStorage()){
					entryQualifier.put(new LeafAttribute(attribute),getSubstance(attribute));
				}
			}
		}
		TransportEntry clonedObject = new TransportEntry(getInternalIdentity(),entryQualifier);
		return clonedObject;
	}

	@Override
	public void reallocate(int size) {
		Substance[] newArray = new Substance[size];
		System.arraycopy(substanceArray, 0, newArray, 0, substanceArray.length);
		substanceArray = newArray;		
	}
}
