package com.biswa.ep.entities.store;

import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.PropertyConstants;

public class ContainerStoreFactory {
	public static ContainerEntryStore getContainerEntryStore(ConcreteContainer concreteContainer){
		
		boolean d3support=false;
		String d3strProperty = concreteContainer.getProperty(PropertyConstants.D3_SUPPORT);
		if(d3strProperty!=null){
			d3support = Boolean.parseBoolean(d3strProperty);
		}
		
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
			if(d3support){
				return new D3PassivableContainerEntryStore(concreteContainer,passivation_idle_period,eager);	
			}else{
				return new PassivableContainerEntryStore(concreteContainer,passivation_idle_period,eager);	
			}		
		}else{			
			if(d3support){
				return new D3ContainerEntryStore(concreteContainer);	
			}else{
				return new ConcreteContainerEntryStore(concreteContainer);
			}
		}	
	}
}
