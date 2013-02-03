package com.biswa.ep.entities;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.biswa.ep.entities.substance.Substance;

public class TransportEntry implements Serializable {
	public static final Map<Attribute,Substance> EMPTY_MAP = Collections.unmodifiableMap(new HashMap<Attribute,Substance>());
	/**
	 * 
	 */
	private static final long serialVersionUID = -1620363036023519197L;
	private final int externalIdentity;
	//Each Attribute and substance are kept here for this record
	protected Map<Attribute, Substance> containerEntryStore;
	
	/**Constructor used by Anonymous servers to make inserts into the container.
	 * 
	 * @param externalIdentity the id by which the rest of the world knows this.
	 */
	public TransportEntry(int externalIdentity){
		this(externalIdentity,EMPTY_MAP);
	}
	
	/**
	 * 
	 * @param externalIdentity the id by which the rest of the world knows this.
	 * @param entryQualifier The qualifying attributes when the record is created.
	 */
	public TransportEntry(int externalIdentity,Map<Attribute, Substance> entryQualifier){
		this.containerEntryStore=entryQualifier;
		this.externalIdentity=externalIdentity;
	}
	
	public int getIdentitySequence() {
		return externalIdentity;
	}

	public Map<Attribute, Substance> getEntryQualifier() {
		return containerEntryStore;
	}
	private Object readResolve() throws ObjectStreamException{
		if(containerEntryStore==null){
			containerEntryStore = EMPTY_MAP;
		}
		return this;
	}

	private Object writeReplace() throws ObjectStreamException{
		if(containerEntryStore==EMPTY_MAP){
			containerEntryStore = null;
		}
		return this;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TransportEntry [externalIdentity=")
				.append(externalIdentity).append(", containerEntryStore=")
				.append(containerEntryStore).append("]");
		return builder.toString();
	}
	
}
