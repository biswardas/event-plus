package com.biswa.ep.entities;


/**The class used to specify what level of concurrency the underlying container supports.
 * In case you want to specify INSERT & UPDATE can be executed concurrently
 * return concurrency support as ConcurrencySupport.INSERT.CONCURRENCY_SUPPORT | ConcurrencySupport.UPDATE.CONCURRENCY_SUPPORT 
 * @author biswa
 *
 */
public enum ConcurrencySupport {
	INSERT(1),DELETE(2),UPDATE(4);
	final int CONCURRENCY_SUPPORT;
	ConcurrencySupport(int input){
		this.CONCURRENCY_SUPPORT=input;
	}
	public int getConcurrencysupport() {
		return CONCURRENCY_SUPPORT;
	}
}