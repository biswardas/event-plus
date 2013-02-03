package com.biswa.ep.entities;

import java.util.HashMap;
import java.util.Map;

import com.biswa.ep.entities.substance.Substance;
/**Join Container Entry hiding the details of container entry implementation.
 * This class should never be initialized explicitly.
 * @author biswa
 *
 */
public class JoinContainerEntry implements ContainerEntry {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1145611334910725426L;
	private final int externalIdentity;
	private final transient JoinContainer cs;
	private final transient Integer left;
	private final transient Integer right;
	 
	/**Constructor
	 * 
	 * @param externalIdentity int
	 * @param cs JoinContainer
	 * @param entryQualifier Map<Attribute, Substance>
	 */
	JoinContainerEntry(int externalIdentity,JoinContainer cs, ContainerEntry left, ContainerEntry right){
		this.externalIdentity = externalIdentity;
		this.cs=cs;
		if(left!=null){
			this.left=left.getIdentitySequence();
		}else{
			this.left=null;
		}
		if(right!=null){
			this.right=right.getIdentitySequence();
		}else{
			this.right=null;
		}
	}

	public ContainerEntry getLeft() {
		return left==null?null:cs.getConcreteEntry(left);
	}

	public ContainerEntry getRight() {
		return right==null?null:cs.getConcreteEntry(right);
	}

	@Override
	public ConcreteContainer getContainer() {
		return cs;
	}

	@Override
	public int getIdentitySequence() {
		return externalIdentity;
	}

	@Override
	public boolean isFiltered(int agentPrimeIdentity) {
		return true;
	}

	@Override
	public void setFiltered(int agentPrimeIdentity, boolean filteredResult) {
		
	}

	@Override
	public TransportEntry cloneConcrete(){
		Map<Attribute, Substance> entryQualifier = new HashMap<Attribute, Substance>();
		//If there is any conflicting attributes then attribute from left schema takes precedence over right
		if(right!=null){
			ContainerEntry rightContainer = getRight();
			for(Attribute rightAttribute:cs.getRightAttributes()){
				entryQualifier.put(rightAttribute, rightContainer.getSubstance(rightAttribute));
			}
		}
		if(left!=null){
			ContainerEntry leftContainer = getLeft();
			for(Attribute leftAttribute:cs.getLeftAttributes()){
				entryQualifier.put(leftAttribute, leftContainer.getSubstance(leftAttribute));
			}
		}
		return new TransportEntry(getIdentitySequence(), entryQualifier);
	}

	@Override
	public int getToClient() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLeftTrueRightFalse() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLeftTrueRightFalse(boolean leftTrueRightFalse) {
		throw new UnsupportedOperationException();		
	}

	@Override
	public Substance getSubstance(Attribute attribute) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Substance silentUpdate(Attribute attribute, Substance substance) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Substance silentUpdate(Attribute attribute, Substance substance,int minor) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void remove(Attribute attribute) {
		throw new UnsupportedOperationException();
		
	}
	@Override
	public void remove(Attribute attribute,int minor) {
		throw new UnsupportedOperationException();
		
	}

	@Override
	public boolean markedAdded() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean markedRemoved() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean markedDirty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void markAdded(boolean flag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void markRemoved(boolean flag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void markDirty(boolean flag) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int touchMode() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getInternalIdentity() {
		return externalIdentity;
	}
}