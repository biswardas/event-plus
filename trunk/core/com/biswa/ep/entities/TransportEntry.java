package com.biswa.ep.entities;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
/**Transport record to construct an entry in Sink container.
 * 
 * @author biswa
 *
 */
public class TransportEntry implements Serializable {
	public static final Map<Attribute, Object> EMPTY_MAP = Collections
			.unmodifiableMap(new HashMap<Attribute, Object>());
	/**
	 * 
	 */
	private static final long serialVersionUID = -1620363036023519197L;
	/**
	 * Container identifier
	 */
	private final int externalIdentity;
	
	/**
	 * Entry Attribute value pair required to create a logical entry.
	 */
	protected Map<Attribute, Object> containerEntryStore;

	/**
	 * Constructor used by Anonymous servers to make inserts into the container.
	 * 
	 * @param externalIdentity
	 *            the id by which the rest of the world knows this.
	 */
	public TransportEntry(int externalIdentity) {
		this(externalIdentity, EMPTY_MAP);
	}

	/**
	 * 
	 * @param externalIdentity
	 *            the id by which the rest of the world knows this.
	 * @param entryQualifier
	 *            The qualifying attributes when the record is created.
	 */
	public TransportEntry(int externalIdentity,
			Map<Attribute, Object> entryQualifier) {
		this.containerEntryStore = entryQualifier;
		this.externalIdentity = externalIdentity;
	}

	/**
	 * Identity of the entry as known to source containers. Sink containers may
	 * use the identity as is or map it to newly generated id.
	 * 
	 * @return identity
	 */
	public int getIdentitySequence() {
		return externalIdentity;
	}

	/**
	 * Map containing attribute value pairs to construct a logical entity in the
	 * sink container. Treat this like arguments to a constructor.
	 * 
	 * @return Map<Attribute, Substance>
	 */
	public Map<Attribute, Object> getEntryQualifier() {
		return containerEntryStore;
	}

	/**
	 * If received null over the network reconstruct the object with EMPTY_MAP.
	 * 
	 * @return Object
	 * @throws ObjectStreamException
	 */
	private Object readResolve() throws ObjectStreamException {
		if (containerEntryStore == null) {
			containerEntryStore = EMPTY_MAP;
		}
		return this;
	}

	/**
	 * Don't bother to send over an empty map over network. Replace it with
	 * null.
	 * 
	 * @return Object
	 * @throws ObjectStreamException
	 */
	private Object writeReplace() throws ObjectStreamException {
		if (containerEntryStore == EMPTY_MAP) {
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
