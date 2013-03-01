package com.biswa.ep.entities;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.substance.InvalidSubstance;
import com.biswa.ep.entities.substance.NullSubstance;
import com.biswa.ep.entities.substance.Substance;

/**
 * This class responsible to define behavior of a attribute in the domain. It
 * defines on what other attribute is dependent on and how to value these
 * attribute when when one of its dependency changes.<br>
 * For example if we have an attribute Z=X+Y then any of the attribute change in
 * X & Y will trigger Z to be valued again.<br>
 * Attributes are compared based on the depth order of the attribute. this
 * attribute will be smaller if the depth corresponding to it is less than the
 * compared one. return 0 if depth is same, 1 if current attribute depth is
 * higher, -1 if current attribute depth is less.
 * 
 * @author biswa
 * 
 */
public abstract class Attribute implements Comparable<Attribute>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4088269181755777782L;

	/**
	 * Returned for ROOT attribute set which does not have any dependency.
	 */
	protected static final Attribute[] ZERO_DEPENDENCY = new Attribute[0];

	/**
	 * Defines constant invalid minor index.
	 */
	protected static final int INVALID_MINOR = Integer.MIN_VALUE;

	/**
	 * Name of the attribute can not be null or space. Keep the name short and
	 * human understandable.
	 */
	final private String name;

	/**
	 * minor is used to identify multi valued substance. if a cell keeps multi
	 * value information then this information is used to track the index of the
	 * value.
	 */
	final private int minor;

	/**
	 * In the graph what is the depth of this attribute
	 */
	transient private int dependencyDepth = 0;

	/**
	 * Tells the ordinal of this attribute in the current container.
	 */
	transient private int ordinal = -1;

	/**
	 * Tells whether this attribute is private. Private attributes are not
	 * propagated to downstream containers.
	 */
	transient private boolean propagate = false;

	/**
	 * This attribute is dependent on following attribute list. The attributes
	 * in the list below are direct dependencies
	 */
	final transient private Collection<Attribute> dependsOnList = new HashSet<Attribute>();

	/**
	 * Attributes dependent on this attribute (including transitively dependent)
	 * in the current container. Don't worry, Do not add anything or remove
	 * anything from here directly. Container reconstructs this information upon
	 * attribute addition and removal
	 */
	transient private Attribute[] dependents = new Attribute[0];

	/**
	 * Variable keeps the state whether this attribute must be initialized on
	 * insertion into the container.
	 */
	transient private boolean initializeOnInsert = true;

	/**
	 * protected constructor to allow creating subclass and create own
	 * Attributes.
	 * 
	 * @param name
	 */
	protected Attribute(String name) {
		this(name, INVALID_MINOR);
	}

	/**
	 * protected constructor to allow creating subclass and create own
	 * Attributes.
	 * 
	 * @param name
	 */
	protected Attribute(String name, int minor) {
		assert name != null && name.trim().length() > 0 : "Can not create Attribute with name null or empty String";
		this.name = name.trim();
		this.minor = minor;
	}

	/**
	 * Returns the name of this attribute
	 * 
	 * @return name
	 */
	final public String getName() {
		return name;
	}

	/**
	 * Sets Ordered number in the container
	 * 
	 * @param ordinal
	 */
	void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	/**
	 * Returns the ordered number in the container.
	 * 
	 * @return int
	 */
	public int getOrdinal() {
		return ordinal;
	}

	/**
	 * Returns whether this attribute requires any storage space in the
	 * container.
	 * 
	 * @return boolean
	 */
	final public boolean requiresStorage() {
		return !isStatic() && !isStateless();
	}

	/**
	 * Method tells should this attribute be initialized upon insertion.
	 * 
	 * @return
	 */
	public boolean initializeOnInsert() {
		return initializeOnInsert;
	}

	/**
	 * Returns the minor index of the attribute.
	 * 
	 * @return minor
	 */
	final public int getMinor() {
		return minor;
	}

	/**
	 * Returns whether this attribute has valid minor
	 * 
	 * @return boolean
	 */
	final public boolean hasMinor() {
		return minor != INVALID_MINOR;
	}

	/**
	 * This method returns vertical propagation property of this attribute.
	 * 
	 * @return boolean
	 */
	public boolean propagate() {
		return propagate;
	}

	/**
	 * This method sets whether this attribute can be propagated. If an
	 * attribute is only transitively added then it is disabled for propagation.
	 * If an attribute is initially added by the container as candidate
	 * attribute however also got qualified to be added due to transitive
	 * dependency followed by candidate removal attribute looses its propagation
	 * status it originally had. The down stream container gets the notification
	 * of attribute being removed however the container keeps it due to its
	 * dependency.
	 * 
	 * @param boolean
	 */
	public void setPropagate(boolean propagate) {
		this.propagate = propagate;
	}

	/**
	 * This method returns whether this attribute is a subscription attribute
	 * 
	 * @return boolean
	 */
	public boolean isSubscription() {
		return false;
	}

	/**
	 * Tells whether this attribute is state less. State less attributes do not
	 * consume memory in the container however the field is recomputed upon
	 * re-subscription or when ever the re-computation of the dependency is
	 * triggered.
	 */
	public boolean isStateless() {
		return false;
	}

	/**
	 * Tells whether this attribute is static.
	 */
	public boolean isStatic() {
		return false;
	}

	/**
	 * Attributes which are dependent on this.
	 * 
	 * @return
	 */
	final Attribute[] getDependents() {
		return dependents;
	}

	/**
	 * Sets the dependents on this attribute.
	 * 
	 * @param dependents
	 */
	final void setDependents(Attribute[] dependents) {
		this.dependents = dependents;
	}

	/**
	 * Defines the dependency of this attribute. This method is defined as
	 * protected so subclasses can define the dependency post attribute
	 * creation. Guest Attribute should not be null. Adding duplicate attribute
	 * will result no operation.
	 * 
	 * @param attribute
	 *            Attribute
	 */
	final public void addDependency(Attribute attribute) {
		assert attribute != null : "Can not add null as dependency";
		if (!this.getName().equals(attribute.getName())) {
			this.dependsOnList.add(attribute);
			dependencyDepth = -1;
		}
	}

	/**
	 * Returns array of attributes on which the current attribute is dependent
	 * on. This method will return a 0 length array of attribute if it is not
	 * dependent on any other attribute. This method must be over ridden to
	 * return its dependency list in the container.
	 * 
	 * @return Attribute[]
	 */
	public Attribute[] dependsOn() {
		return dependsOnList.toArray(ZERO_DEPENDENCY);
	}

	/**
	 * Returns transitive dependency depth of the the current attribute. If the
	 * attribute is not dependent on any other attribute then the dependency
	 * depth will be 0.
	 * 
	 * @return int
	 */
	final int getDependencyDepth() {
		if (dependencyDepth == -1) {
			dependencyDepth = 1;
			for (Attribute attribute : dependsOnList) {
				dependencyDepth = dependencyDepth
						+ attribute.getDependencyDepth();
			}
		}
		return dependencyDepth;
	}

	/**
	 * Links the attribute to the other dependee attributes in the container.
	 * 
	 */
	final void prepare() {
		if (dependsOn().length > 0) {
			HashSet<Attribute> al = new HashSet<Attribute>();
			for (Attribute oneDependee : dependsOn()) {
				Attribute registeredAttribute = oneDependee
						.getRegisteredAttribute();
				al.add(registeredAttribute);
				if (!registeredAttribute.isStatic()) {
					initializeOnInsert = false;
				}
			}
			dependsOnList.clear();
			dependsOnList.addAll(al);
		}
	}

	/**
	 * evaluate the substance representing the attribute in the context
	 * ContainerEntry.
	 * 
	 * @param attribute
	 *            responsible for triggering the evaluation
	 * @param containerEntry
	 *            context on which the this attribute will be evaluated.
	 * @return Substance
	 * @throws Exception
	 */
	protected abstract Substance evaluate(Attribute attribute,
			ContainerEntry containerEntry) throws Exception;

	/**
	 * final template method which routes the evaluate call and handle any
	 * valuation error resulted by actual valuation.
	 * 
	 * @param attribute
	 *            Attribute
	 * @param containerEntry
	 *            ContainerEntry
	 * @return Substance
	 */
	final protected Substance failSafeEvaluate(Attribute attribute,
			ContainerEntry containerEntry) {
		try {
			return evaluate(attribute, containerEntry);
		} catch (Throwable throwable) {
			return NullSubstance.NULL_SUBSTANCE;
		}
	}

	/**
	 * Utility method to find the concrete value in the given container entry
	 * for this attribute
	 * 
	 * @param containerEntry
	 *            ContainerEntry
	 * @return Object
	 */
	final public Object getValue(ContainerEntry containerEntry) {
		return getValue(containerEntry, this);
	}

	/**
	 * Utility method to find the concrete value in the given container entry
	 * for this attribute
	 * 
	 * @param containerEntry
	 *            ContainerEntry
	 * @param name
	 *            String
	 * @return Object
	 */
	protected Object getValue(ContainerEntry containerEntry, String name) {
		return containerEntry.getSubstance(
				containerEntry.getContainer().getAttributeByName(name))
				.getValue();
	}

	/**
	 * Utility method to find the concrete value in the given container entry
	 * for this attribute
	 * 
	 * @param containerEntry
	 *            ContainerEntry
	 * @param attribute
	 *            Attribute
	 * @return Object
	 */
	protected Object getValue(ContainerEntry containerEntry, Attribute attribute) {
		return containerEntry.getSubstance(attribute).getValue();
	}

	/**
	 * Returns the Value of the static attribute.
	 * 
	 * @param attributeName
	 *            String
	 * @return Object
	 */
	final protected Object getStatic(String attributeName) {
		AbstractContainer container = getContainer();
		return container.getStatic(container.getAttributeByName(attributeName))
				.getValue();
	}

	/**
	 * Returns the Value of the static attribute.
	 * 
	 * @param attribute
	 *            Attribute
	 * @return Substance
	 */
	final protected Object getStatic(Attribute attribute) {
		return getContainer().getStatic(attribute).getValue();
	}

	/**
	 * Gets a dependency aware attribute registered in current container.
	 * 
	 * @return Attribute
	 */
	public Attribute getRegisteredAttribute() {
		return getContainer().getAttributeByName(name);
	}

	/**
	 * Returns the associated current container.
	 * 
	 * @return AbstractContainer
	 */
	final public AbstractContainer getContainer() {
		return ContainerContext.CONTAINER.get();
	}

	@Override
	final public int compareTo(Attribute o) {
		if (getDependencyDepth() < o.getDependencyDepth()) {
			return -1;
		} else if (getDependencyDepth() == o.getDependencyDepth()) {
			return 0;
		} else {
			return +1;
		}
	}

	@Override
	final public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	final public boolean equals(Object obj) {
		final Attribute other = (Attribute) obj;
		if (name.equals(other.name) && minor == other.minor)
			return true;
		else
			return false;
	}

	@Override
	final public String toString() {
		if (hasMinor()) {
			return name + "[" + minor + "]";
		} else {
			return name;
		}
	}

	final public String toDependencyString() {
		StringBuilder sb = new StringBuilder("\n");
		sb.append(this);
		sb.append("[depth=");
		sb.append(this.getDependencyDepth());
		sb.append(",propagate=");
		sb.append(propagate());
		sb.append(",static=");
		sb.append(isStatic());
		sb.append(",stateless=");
		sb.append(isStateless());
		if (!isStatic() && !isStateless()) {
			sb.append(",ordinal=");
			sb.append(getOrdinal());
		}
		sb.append(",subscription=");
		sb.append(isSubscription());
		sb.append(",storage=");
		sb.append(requiresStorage());
		sb.append("]");

		sb.append("\n\t");
		sb.append("Attributes I directly depend on[");
		for (Attribute dependent : this.dependsOn()) {
			sb.append(dependent);
			sb.append("[");
			sb.append(dependent.getDependencyDepth());
			sb.append("]");
			sb.append(" ");
		}
		sb.append("]");

		sb.append("\n\t");
		sb.append("Attributes depend on me(including transitive)[");
		for (Attribute dependent : this.getDependents()) {
			sb.append(dependent);
			sb.append("[");
			sb.append(dependent.getDependencyDepth());
			sb.append("]");
			sb.append(" ");
		}
		sb.append("]");
		return sb.toString();
	}
}