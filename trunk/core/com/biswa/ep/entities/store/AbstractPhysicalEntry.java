package com.biswa.ep.entities.store;

import com.biswa.ep.ClientToken;
import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.ConcreteContainer;
/**
 * Manages the common aspects of an entity. meta data client allocation
 * Filtering. New Old status.
 * @author biswa
 *
 */
public abstract class AbstractPhysicalEntry implements PhysicalEntry {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8252260078926037745L;
	private static final int TOUCH_MODE=(ClientToken.ALL_AVAILABLE&MARKED_ADDED)|(ClientToken.ALL_AVAILABLE&MARKED_REMOVED)|(ClientToken.ALL_AVAILABLE&MARKED_DIRTY);
	final int externalIdentity;
	int metainfo;
	
	public AbstractPhysicalEntry(int externalIdentity){
		this.externalIdentity=externalIdentity;
	}
	
	@Override
	final public int getIdentitySequence() {
		return externalIdentity;
	}
	
	@Override
	final public int getToClient() {
		return metainfo;
	}
	
	@Override
	final public boolean isFiltered(int agentPrimeIdentity) {
		return (metainfo&agentPrimeIdentity)>0;
	}
	
	@Override
	final public void setFiltered(int agentPrimeIdentity,boolean filteredResult) {
		if(filteredResult){
			metainfo=metainfo|agentPrimeIdentity;
		}else{			
			metainfo=metainfo&(ClientToken.ALL_AVAILABLE^agentPrimeIdentity);
		}
	}
	
	@Override
	final public boolean isLeftTrueRightFalse() {
		return (metainfo&LEFTORRIGHT)>0;
	}
	
	@Override
	final public void setLeftTrueRightFalse(boolean leftTrueRightFalse) {
		if(leftTrueRightFalse){
			metainfo=metainfo|LEFTORRIGHT;
		}else{			
			metainfo=metainfo&(ClientToken.ALL_AVAILABLE^LEFTORRIGHT);			
		}
	}
	
	@Override
	final public boolean markedAdded() {
		return (metainfo&MARKED_ADDED)>0;
	}

	@Override
	final public boolean markedRemoved() {
		return (metainfo&MARKED_REMOVED)>0;
	}

	@Override
	final public boolean markedDirty() {
		return (metainfo&MARKED_DIRTY)>0;
	}
	
	@Override
	final public void markAdded(boolean flag) {
		reset();
		if(flag){
			metainfo=metainfo|MARKED_ADDED;
		}else{			
			metainfo=metainfo&(ClientToken.ALL_AVAILABLE^MARKED_ADDED);			
		}
	}

	@Override
	final public void markRemoved(boolean flag) {
		reset();
		if(flag){
			metainfo=metainfo|MARKED_REMOVED;
		}else{			
			metainfo=metainfo&(ClientToken.ALL_AVAILABLE^MARKED_REMOVED);			
		}
	}

	@Override
	final public void markDirty(boolean flag) {
		if(flag){
			if(markedAdded()||markedRemoved()){				
			}else{
				metainfo=metainfo|MARKED_DIRTY;
			}
		}else{			
			metainfo=metainfo&(ClientToken.ALL_AVAILABLE^MARKED_DIRTY);			
		}
	}

	@Override
	final public int touchMode() {
		return metainfo&TOUCH_MODE;
	}
	
	@Override
	final public void reset() {
		metainfo=metainfo&(ClientToken.ALL_AVAILABLE^MARKED_ADDED);
		metainfo=metainfo&(ClientToken.ALL_AVAILABLE^MARKED_REMOVED);
		metainfo=metainfo&(ClientToken.ALL_AVAILABLE^MARKED_DIRTY);
	}


	@Override
	final public ConcreteContainer getContainer() {
		return (ConcreteContainer) ContainerContext.CONTAINER.get();
	}

	@Override
	final public int getInternalIdentity() {
		return getContainer()!=null?getContainer().getInternalIdentity(this):externalIdentity;
	}
}
