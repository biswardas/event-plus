package com.biswa.ep.deployment;

import com.biswa.ep.deployment.util.Feedback;
import com.biswa.ep.deployment.util.Filter;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.Predicate;
import com.biswa.ep.entities.spec.FilterSpec;
import com.biswa.ep.entities.transaction.SubscriptionAgent;
import com.biswa.ep.provider.PredicateBuilder;

/**Communication gateway of the containers. This class defines the interfaces in which 
 * two containers can communicate with each other. For example if the SourceContainer 
 * in SourceContext has defined the following publish method.
 * <pre>
 * {@code
 * <Publish method="RMI"/>
 * }
 * </pre>
 * @author biswa
 *
 */
abstract public class Accepter {
	private ContainerManager scm;
	
	/**Constructor which accepts the current schema manager in effect. and provides the
	 * operations for setting up communication infrastructure required for two containers
	 * to communicate.
	 * 
	 * @param scm
	 */
	public Accepter(ContainerManager scm) {
		this.scm = scm;
	}
	
	/**Connect and listen to the specified container. The source listens to
	 * the container specified in the given context using the listening method specified.
	 *<pre>
	 *{@code
	 *<Listen container="FirstContainer" context="FirstContext" method="LOCAL"/>
	 *<Listen container="FirstContainer" context="FirstContext" method="RMI"/>
	 *}
	 *</pre>
	 * This method actually does the connection setting.
	 * @param listen Listen Source container to Listen
	 * @param container CascadeContainer Sink Container
	 */
	public abstract boolean listen(Listen listen, AbstractContainer container);

	
	/**Request the source to replay the data This method does not setup any connection
	 * but just asks the source container to replay the contents when ever required.
	 * 
	 * @param listen Listen Source container to replay
	 * @param container CascadeContainer Sink Container
	 */
	public abstract void replay(Listen listen, AbstractContainer container);
	
	/** The out going method to dispatch events. This method sets up the 
	 * required artifacts in order to be able to dispatch outgoing events
	 * operations and data. This builds the exit door of the container.
	 * <pre>
	 * {@code
	 * <Publish method="RMI"/>
	 * }<pre>
	 * @param cs CascadeSchema Schema to be published
	 */
	public void publish(AbstractContainer cs) {
	}
	
	/** This un publishes the given container from the face of the world.
	 * @param cs CascadeSchema Schema to be unpublished
	 */
	public void unpublish(AbstractContainer cs) {
	}
	
	/**Returns the containerManager in which this Acceptor is operating.
	 * 
	 * @return ContainerManager
	 */
	public ContainerManager getContainerManager() {
		return scm;
	}
	
	/**Build a sink filter to be applicable to underlying sink container
	 * 
	 * @param listen
	 * @return FilterSpec
	 */
	protected FilterSpec buildFilter(Listen listen) {
		//Is there a  filter present?
		Filter filter = listen.getFilter();
		FilterSpec filterSpec = null;
		if(filter!=null){
			Predicate predicate = PredicateBuilder.buildPredicate(filter.getPredicate());
			filterSpec = new FilterSpec(predicate,filter.getChainMode());
		}
		return filterSpec;
	}
	
	protected String feedbackAs(Feedback feedback,AbstractContainer origContainer){
		if(feedback.getAlias()!=null){
			return feedback.getAlias();
		}else{
			return origContainer.getName();
		}
	}
	
	public abstract boolean addFeedbackSource(Feedback feedback, AbstractContainer cs);
	public abstract SubscriptionAgent getSubscriptionAgent(String context,String container);
	public static String buildName(Listen listen){
		return listen.getContext()+"."+listen.getContainer();	
	}
	public static String buildName(Feedback feedback){
		return feedback.getContext()+"."+feedback.getContainer();
	}
}
