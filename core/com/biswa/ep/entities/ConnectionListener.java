package com.biswa.ep.entities;
/**Listener defines method which handles all connection and replay related functionalities.
 * 
 * @author biswa
 *
 */
public interface ConnectionListener extends Source,Sink {
	
}
/**This interface has all methods a source container must support.
 * 
 * @author biswa
 *
 */
interface Source{
	/**Request is sent to source on behalf of sink. This method executed in
	 * the context of Source.
	 * 
	 * @param ce ConnectionEvent
	 */
	public void replay(ConnectionEvent ce);
	
	/**Request is send to source on behalf of sink. This method executed in
	 * the context of Source.
	 * 
	 * @param ce ConnectionEvent
	 */
	public void connect(ConnectionEvent ce);
	/**Request is sent to source on behalf of sink. This method is executed 
	 * in the context of Source.
	 * 
	 * @param ce ConnectionEvent
	 */
	public void disconnect(ConnectionEvent ce);
	
}

/**This interface has all methods a sink container must support.
 * 
 * @author biswa
 *
 */
interface Sink{
	
	/**Method sets expectation on the sink container to expect connection from the sources.
	 * This operation is executed in the context of sink. This is more like educating the sink
	 * to be prepared to receive events from known sources.
	 * @param ce ConnectionEvent
	 */
	public void addSource(ConnectionEvent ce);
	
	/**Source sends the event to sink confirming the connection. This method is
	 * executed in the context of sink.
	 * @param ce ConnectionEvent
	 */
	public void connected(ConnectionEvent ce);
	
	/**Source sends the event to sink confirming the disconnection. This method is
	 * executed in the context of sink.
	 * @param ce ConnectionEvent
	 */
	public void disconnected(ConnectionEvent ce);

	
	/**Method specifies whether the current sink is connected to all its 
	 * pre-known sources.
	 * @return boolean
	 */
	public boolean isConnected();
	
}