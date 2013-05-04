package com.biswa.ep.entities.store;

import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.PropertyConstants;

public class ContainerStoreFactory {
	public static ContainerEntryStore getContainerEntryStore(ConcreteContainer concreteContainer){
		
		int passivation_idle_period = 0;
		String passivateDurStrProperty = concreteContainer.getProperty(PropertyConstants.PASSIVATION_IDLE_DURATION);
		if(passivateDurStrProperty!=null){
			passivation_idle_period = Integer.parseInt(passivateDurStrProperty)*1000;
		}else{
			passivation_idle_period = 0;
		}
		
		if(passivation_idle_period>0){
			boolean eager;
			String wakeupMode = concreteContainer.getProperty(PropertyConstants.PASSIVATION_WAKEUP);
			if(wakeupMode!=null){
				eager = Boolean.parseBoolean(wakeupMode);
			}else{
				eager = true;
			}		
			return new PassivableContainerEntryStore(concreteContainer,passivation_idle_period,eager);
		}else{
			return new ConcreteContainerEntryStore(concreteContainer);
		}	
	}
}
