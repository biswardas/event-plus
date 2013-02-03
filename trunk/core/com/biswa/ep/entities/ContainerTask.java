package com.biswa.ep.entities;

import java.io.Serializable;

import com.biswa.ep.ContainerContext;

/**The Task encompasses operation which will be executed on the underlying container. 
 * It can be extended and used to perform any desired operation on the container. If 
 * a custom insert/delete/update operation is required to be performed then the task 
 * can be sent to container.
 * 
 *
 * @author biswa
 *
 */
public abstract class ContainerTask implements Runnable,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8308585678523489342L;
	final private int rowToBeLocked;
	
	/**
	 * This Constructor builds task which can not be executed
	 * concurrently on the underlying container.
	 */
	public ContainerTask(){
		this.rowToBeLocked=0;
	}
	
	/**This task can be concurrently executed on the underlying container by locking the 
	 * following record.
	 * @param rowToBeLocked int
	 */
	public ContainerTask(int rowToBeLocked){
		this.rowToBeLocked=rowToBeLocked;
	}
	
	/**Record ID which will be locked when the operation is performed on the container.
	 * 
	 * @return int
	 */
	public int getRowToBeLocked() {
		return rowToBeLocked;
	}
	
	/**
	 *Gets access to the underlying container.
	 *@return AbstractContainer 
	 */
	final public AbstractContainer getContainer(){
		return ContainerContext.CONTAINER.get();
	}
	
	@Override
	final public void run(){
		try{
			runtask();
		}catch(Throwable th){
			System.err.println(ContainerContext.CONTAINER.get().getName());
			th.printStackTrace();
		}
	}
	
	protected abstract void runtask() throws Exception;
}