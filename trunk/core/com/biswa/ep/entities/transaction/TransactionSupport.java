package com.biswa.ep.entities.transaction;

/**The interface exposes the method to handle incoming transactions from the
 * rest of world. Merge the multiple source transaction.
 * 
 * @author biswa
 *
 */
public interface TransactionSupport {
	/**Method intercepting the begin transaction.
	 * 
	 * @param te
	 */
	void beginTran(TransactionEvent te);

	/**Method intercepting the commit transaction.
	 * 
	 * @param te
	 */
	void commitTran(TransactionEvent te);

	/**Method intercepting the rollback transaction.
	 * 
	 * @param te
	 */
	void rollbackTran(TransactionEvent te);
	/**
	 * Method responsible to begin a default transaction if an Atomic operation is received
	 * by the underlying container.
	 */
	public void beginDefaultTran();
	
	/**
	 * Method responsible to commit a default transaction if an Atomic operation is committed
	 * by the underlying container.
	 */

	public void commitDefaultTran();
	
	/**
	 * Method responsible to rollback a default transaction if an Atmoic operation fails.
	 */

	public void rollbackDefaultTran();
	
	/**
	 *Method to handle transaction timeout. 
	 */
	void transactionTimedOut();
	
	/**Time before transaction is timed out. <=0 is no timeout.
	 * 
	 * @return long
	 */
	long getTimeOutPeriodInMillis();
}
