package com.biswa.ep.subscription;

import com.biswa.ep.entities.ContainerEntry;
import com.biswa.ep.entities.substance.Substance;
/**An handler interface to dispatch subscription requests.
 * 
 * @author biswa
 *
 */
public interface AttributeSubscription {

	/**Subscribes a subject for the current entity
	 * 
	 * @param  subscriptionAttribute SubscriptionAttribute 
	 * @param  containerEntry ContainerEntry
	 * @return Substance
	 */
	Substance subscribe(SubscriptionAttribute subscriptionAttribute, ContainerEntry containerEntry);
	/**Unsubscribes a subject for this container entity
	 * 
	 * @param  subscriptionAttribute SubscriptionAttribute 
	 * @param  containerEntry ContainerEntry
	 * @return Substance
	 */
	Substance unsubscribe(SubscriptionAttribute subscriptionAttribute, ContainerEntry containerEntry);
	
	/**replaces any previous subscription and subscribes to
	 * new subject.
	 * @param  subscriptionAttribute SubscriptionAttribute 
	 * @param  containerEntry ContainerEntry
	 * @return Substance
	 */
	Substance substitute(SubscriptionAttribute subscriptionAttribute, ContainerEntry containerEntry);
}
