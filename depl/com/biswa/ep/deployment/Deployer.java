package com.biswa.ep.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.biswa.ep.NamedThreadFactory;
import com.biswa.ep.UncaughtExceptionHandler;
import com.biswa.ep.deployment.mbean.ConMan;
import com.biswa.ep.deployment.mbean.ConManMBean;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Param;
import com.biswa.ep.discovery.Binder;
import com.biswa.ep.discovery.RegistryHelper;

public class Deployer extends UncaughtExceptionHandler{	
	private static final ContainerManager containerManager = new ContainerManager();

	private static EPDeployerImpl deployerImpl = null;
	private static EPDeployer exportedDeployer = null;
	
	private static final String DEPLOYMENT_DESC = "deployment.desc";

	final static ExecutorService deployer = Executors
			.newSingleThreadExecutor(new NamedThreadFactory("Deployer",false));

	//public final static ContainerManager containerManager = new ContainerManager();
	final static MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	
	public static void main(String[] args) throws JAXBException {
		//Register this with mbean server
		ConManMBean csMbean = new ConMan();
		try {
			Deployer.mbs.registerMBean(csMbean, new ObjectName("RootDeployer:name=Root"));
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Error deploying root deployer with JMX");
		}
		
		String fileName = System.getProperty(DEPLOYMENT_DESC);
		if(fileName!=null){
			deploy(fileName);
		}else{
			deployer.execute(new Runnable() {				
				@Override
				public void run() {
					if(getSufix()!=null){
						System.err.println("Starting as Slave. (-Ddeployment.desc=$fileName)");
						try {
							Binder binder = RegistryHelper.getBinder();
							deployerImpl = new EPDeployerImpl();
							exportedDeployer = (EPDeployer) UnicastRemoteObject
									.exportObject(deployerImpl, 0);
							binder.bind(getSufix(), exportedDeployer);
						} catch (RemoteException e) {
							throw new RuntimeException(e);
						}
					}else{
						System.err.println("Slave mode not enabled.(-Dep.engine.name=$engineName) (-Ddeployment.desc=$fileName)");		
						System.exit(0);
					}
				}
			});
		}
	}


	public static void deploy(String fileName) throws JAXBException {
		System.out.println("Attempting to deploy "+fileName);
		Context context = buildContext(fileName);
		deploy(context,false);
		System.out.println(fileName+" deployed.");
	}
	
	
	@SuppressWarnings("unchecked")
	public static Context buildContext(String fileName) throws JAXBException{
		JAXBContext jc = JAXBContext
				.newInstance("com.biswa.ep.deployment.util");

		Unmarshaller unmarshaller = jc.createUnmarshaller();
		InputStream ins = null;
		JAXBElement<Context> rootObj = null;
		try{
			File file = new File(fileName);
			if(file.exists()){
				ins = new FileInputStream(file);
			}else{
				ins = ClassLoader.getSystemResourceAsStream(fileName);
			}
			rootObj = (JAXBElement<Context>) unmarshaller
					.unmarshal(ins);
		}catch(Exception e){
			throw new RuntimeException(e);
		}finally{
			try {
				if(ins!=null){
					ins.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return (Context) rootObj.getValue();
	}

	public static Future<?> deploy(final Context context,boolean sorted) {
		containerManager.registerWithManagementServer(context);
		return deployer.submit(new DeploymentTask(context,containerManager,sorted));
	}

	public static Properties getProperties(List<Param> params) {
		Properties props = new Properties();
		for(Param oneParam:params){
			props.put(oneParam.getName(), oneParam.getValue());
		}
		return props;
	}

	public static String getSufix() {
		return System.getProperty("ep.engine.name");
	}
}
