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
	 *Method to handle transaction timeout. 
	 */
	void transactionTimedOut();
	
	/**Time before transaction is timed out. <=0 is no timeout.
	 * 
	 * @return long
	 */
	long getTimeOutPeriodInMillis();
}
