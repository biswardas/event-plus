package com.biswa.ep.deployment.mbean;

import javax.xml.bind.JAXBException;

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
				throw new RuntimeException("Error deploying add on graph");
			}
		}else{
			try {
				Deployer.deploy(fileName);
			} catch (JAXBException e) {
				throw new RuntimeException("Error while creating new graph");
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
}
