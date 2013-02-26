package com.biswa.ep.entities;

import com.biswa.ep.EPEvent;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.transaction.Agent;

/**The event which is sent on behalf of sink to source requesting connection.<br>
 * The event is also sent from source to sink confirming the connected.
 * @author biswa
 *
 */
public class ConnectionEvent extends EPEvent {

	
	private static final int DEFAULT_TRANSACTION_GROUP = 1;
	/**
	 * 
	 */
	private static final long serialVersionUID = -3416659460957697473L;
	/**
	 * Container listeners are nullified when they leave the virtual machine 
	 */
	private transient Agent dcl;

	private String sink;
	
	/**
	 * Connect to source container with the following filterspec.Remember this filter will ONLY apply to 
	 * requesting sink in addition to the global filter applied to the source.
	 */
	private FilterSpec filterSpec;
	
	/**The transaction group number this source belongs to this sink. Sink may be
	 * listening to 4 sources at a time i.e A,B,C,D
	 * where as 
	 * A,B can belong to transaction group 1
	 * C,D can belong to transaction group 2
	 * 
	 */
	private int transactionGroup;

	/**Constructor used when Connection Requested event is sent to Source container.
	 * Connection requested must use this constructor and a valid Agent
	 * of the sink container.
	 * @param source String name of the source 
	 * @param sink String  name of the sink
	 * @param transactionGroup the transaction group this source belongs the sink do care about
	 * @param dcl Agent dynamic container listener of the sink.
	 * @param filterSpec Filter applied to this sink
	 */
	public ConnectionEvent(String source,String sink,int transactionGroup, Agent dcl,FilterSpec filterSpec) {
		super(source);
		this.sink = sink;
		this.transactionGroup=transactionGroup;
		this.dcl = dcl;
		this.filterSpec=filterSpec;
	}		
	
	/**Constructor used when Connection Requested event is sent to Source container.
	 * Connection requested must use this constructor and a valid Agent
	 * of the sink container.
	 * @param source String name of the source 
	 * @param sink String  name of the sink
	 * @param dcl Agent dynamic container listener of the sink.
	 */
	public ConnectionEvent(String source,String sink, Agent dcl) {
		this(source, sink,DEFAULT_TRANSACTION_GROUP, dcl,null);
	}		
	
	
	/**Constructor used when Connection Requested event is sent to Source container.
	 * Including a filter to be applied to the source container.
	 * Connection requested must use this constructor and a valid Agent
	 * of the sink container.
	 * @param source String name of the source 
	 * @param sink String  name of the sink
	 * @param dcl Agent dynamic container listener of the sink.
	 * @param filterSpec Filter applied to this sink
	 */
	public ConnectionEvent(String source,String sink, Agent dcl,FilterSpec filterSpec) {
		this(source, sink,DEFAULT_TRANSACTION_GROUP, dcl,filterSpec);
	}		


	
	/**Shallow version of the event used when the replay is requested and
	 * the connection confirmation is sent.
	 * @param source String source which serving the information 
	 * @param sink String sink which consumes the information
	 */
	public ConnectionEvent(String source,String sink) {
		this(source,sink,DEFAULT_TRANSACTION_GROUP,null,null);
	}
	
	/**Shallow version of the event used when the replay is requested and
	 * the connection confirmation is sent.
	 * Including a filter to be applied to the source container.
	 * @param source String source which serving the information 
	 * @param sink String sink which consumes the information
	 */
	public ConnectionEvent(String source,String sink,FilterSpec filterSpec) {
		this(source,sink,DEFAULT_TRANSACTION_GROUP,null,filterSpec);
	}	

	/**Shallow version of the event used when source expectation is set
	 * @param source String source which serving the information 
	 * @param sink String sink which consumes the information
	 * @param transactionGroup the transaction group this source belongs the sink do care about
	 */
	public ConnectionEvent(String source,String sink,int transactionGroup) {
		this(source,sink,transactionGroup,null,null);
	}
	
	/**
	 * Return the transaction group of this source.
	 * @return int
	 */
	public int getTransactionGroup() {
		return transactionGroup;
	}

	/**
	 * @return String Returns the fully qualified Sink name
	 */
	public String getSink(){
		return sink;
	}
	
	/**
	 * 
	 * @return FilterSpec applicable in the given connection request.
	 */
	public FilterSpec getFilterSpec() {
		return filterSpec;
	}

	/**
	 * 
	 * @return Agent Returns the Agent of the sink
	 */
	public Agent getAgent(){
		return dcl;
	}

	@Override
	public String toString() {
		return "ConnectionEvent [source=" + getSource() + ", sink=" + sink
				+ ", transactionGroup=" + transactionGroup + "]";
	}
	
}
