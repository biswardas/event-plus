package com.biswa.ep.entities;
/**
 * Properties name class containing properties which can be used to fine
 * tune the property of a Container.
 * @author biswa
 *
 */
public interface PropertyConstants {
	
	/**
	 * Enables printing debug messages on container.
	 */
	String VERBOSE = "verbose";
	
	/**
	 * Concurrent property of the container. this is a BitMap integer constant each bit specifying the 
	 * concurrent nature of the operation.
	 */
	String CONCURRENT = "concurrent";
	
	/**
	 * Property which specifies when the transaction begins is it a eager container or a lazy container.
	 * A lazy container is one which begins transaction only when all upstream container commits the 
	 * transaction. 
	 */
	String BEGIN_ON_COMMIT = "begin.on.commit";
	
	/**
	 * Transaction timeout duration. The time after which a transaction is timed out allowing other
	 * transaction in queue to be processed.
	 */
	String TRAN_TIME_OUT = "tran.time.out";
	
	/**
	 * Expected number of entries in the container
	 */
	String EXPECTED_ROW_COUNT = "expected.row.count";
	
	/**
	 * Expected number of entries in the container
	 */
	String EXPECTED_COLUMN_COUNT = "expected.column.count";
	
	/**
	 * Resolve identity conflicts(true/false)
	 */
	String RESOLVE_IDENTITY_CONFLICTS = "resolve.identity.conflicts";
	
	/**
	 * Mem Optimization(Hashmap loadfactor)
	 */
	String MEM_OPTIMIZATION = "memory.optimization";
	
	/**
	 * D3 Support Enabled
	 */
	String D3_SUPPORT= "d3.enabled";
	
	/**
	 * Passivation after certain time
	 */
	String PASSIVATION_IDLE_DURATION="passivation_idle_period";
	
	/**
	 * Wake up mode 
	 * Allowed values
	 * true - eager
	 * false - lazy
	 */
	String PASSIVATION_WAKEUP="passivation_wakeup";
	
	/**
	 * Property which tells how to react when a duplicate entry is attempted to be inserted.
	 * Allowed values:
	 * 0-Delete & Reinsert (Default)
	 * 1-Merge
	 * 2-Ignore
	 */
	String DUPLICATE_INSERT_MODE="duplicate_insert_mode";
	
	/**
	 *Worker thread count for this container. 
	 */
	String WORKER_THREAD_COUNT = "worker_thread_count";

	/**
	 *Feedback timeout in seconds
	 */
	String FEEDBACK_TIME_OUT = "feedback_time_out";

	/**
	 * Maximum number of entries per client, applicable on split containers.
	 */
	String MAX_PER_CLIENT = "max_per_client";
	
	/**
	 * Timed Interval for the timed container
	 */
	String TIMED_INTERVAL = "timed.interval";
	
	/**
	 * Transforms the substance value in proxy container.
	 * Ignored in other containers.
	 */
	String PROXY_VALUE_TRANSFORMER = "proxy.value.transformer";
	
	/**
	 * Boolean property to deny alien attributes. 
	 * Only attributes added in current container cross past this point.
	 */
	String ALLOW_ALIEN_ATTRIBUTES = "allow.alien.attributes";
	/**
	 * Number recommended slave processes to use. defaults to 2 in absence. max allowed 31.
	 * 
	 */
	String EP_SLAVE_COUNT = "ep.slave.count";
	
	public int concurrencySupport();
	public int getTimeOutPeriodInMillis();
	public boolean beginOnCommit();
}
