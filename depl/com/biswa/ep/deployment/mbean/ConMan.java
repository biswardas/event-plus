package com.biswa.ep.deployment.mbean;

import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.Deployer;

public class ConMan implements ConManMBean {
	private ContainerManager containerManager;
	public ConMan() {
	}
	public ConMan(ContainerManager containerManager) {
		this.containerManager=containerManager;
	}

	@Override
	public void createOrMergeGraph(String fileName) {
		if(containerManager!=null){
			try{
				containerManager.mergeGraph(fileName);
			}catch(Exception e){
				throw new RuntimeException("Error deploying add on graph",e);
			}
		}else{
			try {
				Deployer.deploy(fileName);
			} catch (Exception e) {
				throw new RuntimeException("Error while creating new graph",e);
			}
		}
	}
	@Override
	public String destroy() {
		if(containerManager==null){
			return "Can not destroy root context....";
		}
		containerManager.destroyAllContainers();
		return "Attempting to destroy this Context";
	}
	@Override
	public String shutDown() {
		if(containerManager!=null){
			return "Can not shut Down from Non Root Context..";
		}
		Deployer.shutDown();
		return "Shutting Down in 5 Seconds...";
	}
}
