package com.biswa.ep.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.biswa.ep.entities.substance.Substance;
/**Join Container takes input from two containers L,R and try to create join entries for
 * down stream containers based on the relational Join Policy.
 * 
 * @author biswa
 *
 */
final public class JoinContainer extends ConcreteContainer {
	public enum JoinPolicy {
		INNER_JOIN, LEFT_JOIN, RIGHT_JOIN, OUTER_JOIN
	}
	/**
	 * Left Container Name
	 */
	private String leftContainerName = null;
	/**
	 * Right Container Name
	 */
	private String rightContainerName = null;
	/**
	 * Left Container attributes
	 */
	private Collection<Attribute> leftAttributes = new HashSet<Attribute>();
	/**
	 * Right Container attributes
	 */
	private Collection<Attribute> rightAttributes = new HashSet<Attribute>();
	/**
	 * Join Policy for this container
	 */
	private JoinPolicy joinPolicy;
	/**
	 * Index maintaining the joined entries
	 */
	private JoinedEntryIndex jeIndex;
	
	/**
	 * Entries which are seen to the downstream containers.
	 */
	private Collection<JoinedEntryIndex.JoinedEntry> joinedEntryCollection = new ArrayList<JoinedEntryIndex.JoinedEntry>();


	private boolean leftTrueRightFalse = false;
	/**
	 * Constructor to create a Joined Container based on the specification
	 * @param name Container Name
	 * @param joinPolicy JoinPolicy
	 * @param comparator Matcher for the left and right entries
	 * @param leftContainerName Left container name
	 * @param rightContainerName Right container name
	 * @param props Properties for the container
	 */
	public JoinContainer(String name, JoinPolicy joinPolicy,
			Comparator<ContainerEntry> comparator, 
			String leftContainerName, String rightContainerName,Properties props) {
		super(name,props);
		this.leftContainerName = leftContainerName;
		this.rightContainerName = rightContainerName;
		this.joinPolicy = joinPolicy;
		jeIndex = new JoinedEntryIndex(comparator);
	}

	@Override
	public void entryAdded(ContainerEvent ce) {
		if (ce.getSource().equals(leftContainerName)) {
			leftTrueRightFalse = true;
		}else{
			leftTrueRightFalse = false;
		}
		super.entryAdded(ce);
	}

	@Override
	public void dispatchEntryAdded(ContainerEntry ce){
		ce.setLeftTrueRightFalse(leftTrueRightFalse);
		createJoin(ce);
	}
	
	@Override
	public void dispatchEntryRemoved(ContainerEntry ce){
		if(ce.isLeftTrueRightFalse()){
			jeIndex.removeLeftJoinedEntry(ce);
		}else{
			jeIndex.removeRightJoinedEntry(ce);
		}
		
	}
	
	@Override
	public void dispatchEntryUpdated(Attribute attribute, Substance substance, ContainerEntry containerEntry){
		jeIndex.updateJoinedEntry(attribute,substance,containerEntry);
	}
	
	@Override
	public void attributeAdded(ContainerEvent ce) {
		super.attributeAdded(ce);
		if (ce.getSource().equals(leftContainerName)) {
			leftAttributes.add(ce.getAttribute().getRegisteredAttribute());
		} else if (ce.getSource().equals(rightContainerName)) {
			rightAttributes.add(ce.getAttribute().getRegisteredAttribute());
		}
	}

	@Override
	public void attributeRemoved(ContainerEvent ce) {
		if (ce.getSource().equals(leftContainerName)) {
			leftAttributes.remove(ce.getAttribute());
		} else {
			rightAttributes.remove(ce.getAttribute());
		}
		super.attributeRemoved(ce);
	}
	
	public Collection<Attribute> getLeftAttributes() {
		return leftAttributes;
	}

	public Collection<Attribute> getRightAttributes() {
		return rightAttributes;
	}
	
	@Override
	final public ContainerEntry[] getContainerEntries() {
		Collection<ContainerEntry> containerEntries= new ArrayList<ContainerEntry>();
		for(JoinedEntryIndex.JoinedEntry je :joinedEntryCollection){
			containerEntries.add(je.joinedEntry);
		}
		return containerEntries.toArray(new ContainerEntry[0]);
	}
	
	@Override
	final public int getEntryCount() {
		return joinedEntryCollection.size();
	}
	
	/**Method performs the join/rejoin post insert. 
	 * 
	 * @param ce
	 */
	private void createJoin(ContainerEntry ce) {
		int count=0;
		if (ce.isLeftTrueRightFalse()) {
			//Join driven by entry into the left container.
			for (ContainerEntry rightSchemaEntry : super.getContainerDataEntries()) {
				if (!rightSchemaEntry.isLeftTrueRightFalse()) {
					if(jeIndex.addLeftJoinedEntry(ce, rightSchemaEntry)!=null){
						count++;
					}
				}
			}
			if(count==0){
				//No match found is the container a left outer join / full outer join
				if(joinPolicy==JoinPolicy.LEFT_JOIN || joinPolicy==JoinPolicy.OUTER_JOIN){ 
					jeIndex.addLeftJoinedEntry(ce);
				}
			}
		} else {
			//Join driven by entry into right container
			for (ContainerEntry leftSchemaEntry : super.getContainerDataEntries()) {
				if (leftSchemaEntry.isLeftTrueRightFalse()) {
					if(jeIndex.addRightJoinedEntry(leftSchemaEntry, ce)!=null){
						count++;
					}
				}
			}
			if(count==0){
				//No match found is the container a right outer join / full outer join
				if(joinPolicy==JoinPolicy.RIGHT_JOIN || joinPolicy==JoinPolicy.OUTER_JOIN){
					jeIndex.addRightJoinedEntry(ce);
				}
			}
		}
	}
	/**Manages all the joined entries which are seen to the down stream containers. There
	 * will be just one instance of this for every join container.
	 * 
	 * @author biswa
	 *
	 */
	final class JoinedEntryIndex {
		/**
		 * Index which maintains all the links to left / right. 
		 */
		private HashMap<ContainerEntry, Set<JoinedEntry>> joinIndex = new HashMap<ContainerEntry, Set<JoinedEntry>>();
		
		/**
		 * Comparator which decides whether the entry can be joined.
		 */
		private Comparator<ContainerEntry> comparator;

		/**Constructor
		 * @param comparator
		 */
		private JoinedEntryIndex(Comparator<ContainerEntry> comparator) {
			this.comparator = comparator;
		}
		
		/**Data entry update. For each update from upstream container it multiplies
		 * to all the joined entries.
		 * 
		 * @param attribute Attribute
		 * @param substance Substance
		 * @param containerEntry ContainerEntry
		 */
		private void updateJoinedEntry(Attribute attribute, Substance substance,
				ContainerEntry containerEntry) {
			Set<JoinedEntry> updatedEntries = joinIndex.get(containerEntry);
			if(updatedEntries!=null){
				for(JoinedEntry joinedEntry:updatedEntries){
					joinedEntry.updateContainerEntry(attribute, substance);
				}
			}
		}		
		
		/** Creates an right outer joined entry
		 * 
		 * @param containerEntry ContainerEntry
		 * @return JoinedEntry
		 */
		private JoinedEntry addRightJoinedEntry(ContainerEntry containerEntry) {
			return new JoinedEntry(null,containerEntry);			
		}
		/**Creates an left outer joined entry
		 * 
		 * @param containerEntry ContainerEntry
		 * @return JoinedEntry
		 */
		private JoinedEntry addLeftJoinedEntry(ContainerEntry containerEntry) {
			return new JoinedEntry(containerEntry, null);			
		}
		/**Creates an joined entry if possible, Right was already there left just arrived.
		 * 
		 * @param left ContainerEntry
		 * @param right ContainerEntry
		 * @return JoinedEntry
		 */
		private JoinedEntry addLeftJoinedEntry(ContainerEntry left, ContainerEntry right) {
			JoinedEntry joinedEntry = null;
			if (comparator.compare(left, right) == 0) {
				//Check if the right has a solo entry if so then remove it 
				if(joinPolicy==JoinPolicy.RIGHT_JOIN||joinPolicy==JoinPolicy.OUTER_JOIN){
					Set<JoinedEntry> priorEntries = joinIndex.get(right);
					if(priorEntries.size()==1){
						for(JoinedEntry priorEntry:priorEntries){
							if(priorEntry.getLeft()==null){
								priorEntry.removeRightJoinedEntry(right);
								joinIndex.remove(right);
							}
						}
					}
				}
				//Create the joined entry 
				joinedEntry = new JoinedEntry(left, right);
			}
			return joinedEntry;
		}
		
		/**Creates an joined entry if possible, Left entry was already present. Right just arrived.
		 * 
		 * @param left ContainerEntry
		 * @param right ContainerEntry
		 * @return JoinedEntry
		 */
		private JoinedEntry addRightJoinedEntry(ContainerEntry left, ContainerEntry right) {
			JoinedEntry joinedEntry = null;
			if (comparator.compare(left, right) == 0) {
				//Check if the left has a solo entry if so then remove it
				if(joinPolicy==JoinPolicy.LEFT_JOIN||joinPolicy==JoinPolicy.OUTER_JOIN){
					Set<JoinedEntry> priorEntries = joinIndex.get(left);
					if(priorEntries.size()==1){
						for(JoinedEntry priorEntry:priorEntries){
							if(priorEntry.getRight()==null){
								priorEntry.removeLeftJoinedEntry(left);
								joinIndex.remove(left);
							}
						}
					}
				}
				//Create the joined entry
				joinedEntry = new JoinedEntry(left, right);
			}
			return joinedEntry;
		}
		
		/**The entry will remove all the join entries.
		 * 
		 * @param containerEntry ContainerEntry
		 */
		private void removeLeftJoinedEntry(ContainerEntry containerEntry){
			Set<JoinedEntry> joinEntries = joinIndex.get(containerEntry);
			for(JoinedEntry joinEntry:joinEntries){
				//remove the joined entry
				joinEntry.removeLeftJoinedEntry(containerEntry);
				//Check if removal of this entry gives birth to any solo entries
				if(joinPolicy==JoinPolicy.RIGHT_JOIN || joinPolicy==JoinPolicy.OUTER_JOIN){
					if(joinEntry.getRight()!=null){
						Set<JoinedEntry> rightJoinedEntries = joinIndex.get(joinEntry.getRight());
						if(rightJoinedEntries.isEmpty()){
							addRightJoinedEntry(joinEntry.getRight());
						}
					}
				}				
			}
			joinIndex.remove(containerEntry);
		}
		
		/**The entry will remove all the join entries.
		 * 
		 * @param containerEntry ContainerEntry
		 */
		private void removeRightJoinedEntry(ContainerEntry containerEntry){
			Set<JoinedEntry> joinEntries = joinIndex.get(containerEntry);
			for(JoinedEntry joinEntry:joinEntries){
				//Remove the joined entry
				joinEntry.removeRightJoinedEntry(containerEntry);
				//Check if removal of this gives birth to any solo entries
				if(joinPolicy==JoinPolicy.LEFT_JOIN || joinPolicy==JoinPolicy.OUTER_JOIN){
					if(joinEntry.getLeft()!=null){
						Set<JoinedEntry> leftJoinedEntries = joinIndex.get(joinEntry.getLeft());
						if(leftJoinedEntries.isEmpty()){
							addLeftJoinedEntry(joinEntry.getLeft());
						}
					}
				}
			}
			joinIndex.remove(containerEntry);
		}
		
		/**Concrete entry for each matching from the upstream containers.
		 * 
		 * @author biswa
		 *
		 */
		final class JoinedEntry {
			/**
			 * The entry which is propagated to downstream containers
			 */
			final private JoinContainerEntry joinedEntry;
			/**
			 * Creates one container join container entry from the left and right
			 * 
			 * @param left
			 * @param right
			 */
			private JoinedEntry(ContainerEntry left, ContainerEntry right) {
				if (left != null)
					updateIndex(left);
				if (right != null)
					updateIndex(right);
				
				joinedEntryCollection.add(this);
				joinedEntry = new JoinContainerEntry(generateIdentity(),left,right);
				JoinContainer.super.dispatchEntryAdded(joinedEntry);
			}

			private void updateIndex(ContainerEntry indexEntry) {
				Set<JoinedEntry> coll = joinIndex.get(indexEntry);
				if (coll == null) {
					coll = new HashSet<JoinedEntry>();
					joinIndex.put(indexEntry, coll);
				}
				coll.add(this);
			}
			
			/** Updates the container entry.
			 * 
			 * @param attribute
			 * @param substance
			 */
			private void updateContainerEntry(Attribute attribute,
					Substance substance) {
				JoinContainer.super.dispatchEntryUpdated(attribute, substance, joinedEntry);				
			}
			
			/**Removes the container entry from the down stream containers.
			 * 
			 * @param containerEntry
			 */
			private void removeLeftJoinedEntry(ContainerEntry containerEntry) {
				if(getRight()!=null){
					Set<JoinedEntry> coll = joinIndex.get(getRight());
					coll.remove(this);
				}
				joinedEntryCollection.remove(this);
				JoinContainer.super.dispatchEntryRemoved(joinedEntry);				
			}
			
			/**Removes the container entry from the down stream containers.
			 * 
			 * @param containerEntry
			 */
			private void removeRightJoinedEntry(ContainerEntry containerEntry) {
				if(getLeft()!=null){
					Set<JoinedEntry> coll = joinIndex.get(getLeft());
					coll.remove(this);
				}
				joinedEntryCollection.remove(this);
				JoinContainer.super.dispatchEntryRemoved(joinedEntry);				
			}

			
			/**Returns the right entry for the concrete Container Entry
			 * 
			 * @return ContainerEntry
			 */
			private ContainerEntry getRight() {
				return joinedEntry.getRight();
			}
			
			/**Returns the left entry for the concrete Container Entry
			 * 
			 * @return ContainerEntry
			 */
			private ContainerEntry getLeft() {
				return joinedEntry.getLeft();
			}
		}
	}
}
