package com.biswa.ep.entities;

import java.util.HashMap;
import java.util.Map;
import com.biswa.ep.entities.store.AbstractPhysicalEntry;
import com.biswa.ep.entities.substance.Substance;
/**Join Container Entry hiding the details of container entry implementation.
 * This class should never be initialized explicitly.
 * @author biswa
 *
 */
public class JoinContainerEntry extends AbstractPhysicalEntry {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1145611334910725426L;
	private final transient Integer left;
	private final transient Integer right;
	 
	/**Constructor
	 * 
	 * @param externalIdentity int
	 * @param cs JoinContainer
	 * @param entryQualifier Map<Attribute, Substance>
	 */
	JoinContainerEntry(int externalIdentity, ContainerEntry left, ContainerEntry right){
		super(externalIdentity);
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
		return left==null?null:cs().getConcreteEntry(left);
	}

	public ContainerEntry getRight() {
		return right==null?null:cs().getConcreteEntry(right);
	}

	public JoinContainer cs(){
		return (JoinContainer)super.getContainer();
	}
	
	@Override
	public TransportEntry cloneConcrete(){
		Map<Attribute, Substance> entryQualifier = new HashMap<Attribute, Substance>();
		//If there is any conflicting attributes then attribute from left schema takes precedence over right
		if(right!=null){
			ContainerEntry rightContainer = getRight();
			for(Attribute rightAttribute:cs().getRightAttributes()){
				entryQualifier.put(rightAttribute, rightContainer.getSubstance(rightAttribute));
			}
		}
		if(left!=null){
			ContainerEntry leftContainer = getLeft();
			for(Attribute leftAttribute:cs().getLeftAttributes()){
				entryQualifier.put(leftAttribute, leftContainer.getSubstance(leftAttribute));
			}
		}
		return new TransportEntry(getIdentitySequence(), entryQualifier);
	}

	@Override
	public Substance getSubstance(Attribute attribute) {
		JoinContainer cs = cs();
		if(cs.getLeftAttributes().contains(attribute)){
			return cs.getConcreteEntry(left).getSubstance(attribute);
		}else{
			return cs.getConcreteEntry(right).getSubstance(attribute);
		}
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
	public void reallocate(int size) {
		throw new UnsupportedOperationException();		
	}
}