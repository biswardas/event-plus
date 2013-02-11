package com.biswa.ep.discovery;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.subscription.SubscriptionEvent;
/**Connecter lives in the context  of the source which is responsible to
 * create a proxy RMIRemoteContainer in the remote context and dispatch the updates
 * to respective lister.
 * 
 * @author biswa
 *
 */
public interface Connector extends Remote  {
	/**Method sets up the connection with remote container and start initiating 
	 * data connection.
	 * @param source String source to which the connection request is made
	 * @param sink String target the container which requesting connection.
	 * @param filter Filter applied to this sink
	 * @throws RemoteException
	 */
	void connect(String source,String sink,FilterSpec filter) throws RemoteException;
	
	/**Method to disconnect the remote container from the source. Method is dispatched 
	 * from client GUI or a client Context.
	 * @param source String source to which the connection request is made
	 * @param sink String target the container which requesting connection.
	 * @throws RemoteException
	 */
	void disconnect(String source, String sink) throws RemoteException;

	/**Method request to replay all contents.
	 * @param source String source to which the connection request is made
	 * @param sink String target the container which requesting connection.
	 * @param filter Filter applied to this sink
	 * @throws RemoteException
	 */
	void replay(String source, String sink,FilterSpec filter) throws RemoteException;

	/**Method request to set feedback source on the interested parties
	 * @param consumer String the consumer of the feedback
	 * @param producer String producer of the feedback
	 * @throws RemoteException
	 */

	void addFeedbackSource(String consumer, String producer) throws RemoteException;


	/**Method relay the feedback from the originating container to
	 * source container.
	 * @param consumer String the consumer of the feedback
	 * @param producer String producer of the feedback
	 * @param transactionID transactionID
	 * @throws RemoteException
	 */

	void receiveFeedback(String consumer, String producer,int transactionID) throws RemoteException;
	
	/**Invokes subscription on subscription container.
	 * 
	 * @param subscriptionEvent SubscriptionEvent 
	 */
	void subscribe(SubscriptionEvent subscriptionEvent) throws RemoteException;
	
	/**
	 * Invokes un subscribe on subscription container.
	 * 
	 * @param subscriptionEvent SubscriptionEvent 
	 */
	void unsubscribe(SubscriptionEvent subscriptionEvent) throws RemoteException;

	/**
	 * Invokes substitute on subscription container.
	 * 
	 * @param subscriptionEvent SubscriptionEvent 
	 */
	void substitute(SubscriptionEvent subscriptionEvent) throws RemoteException;

}
