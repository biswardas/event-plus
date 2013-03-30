package com.biswa.ep.deployment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.biswa.ep.NamedThreadFactory;
import com.biswa.ep.UncaughtExceptionHandler;
import com.biswa.ep.deployment.mbean.ConMan;
import com.biswa.ep.deployment.mbean.ConManMBean;
import com.biswa.ep.deployment.util.Context;
import com.biswa.ep.deployment.util.Param;
import com.biswa.ep.discovery.Binder;
import com.biswa.ep.discovery.DiscProperties;
import com.biswa.ep.discovery.RegistryHelper;

public class Deployer extends UncaughtExceptionHandler implements DiscProperties{
	final static ExecutorService deployer = Executors
			.newSingleThreadExecutor(new NamedThreadFactory("Deployer",false));

	final static MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();	
	
	private static final ContainerManager containerManager = new ContainerManager();
	
	private static EPDeployerImpl deployerImpl = null;
	private static EPDeployer exportedDeployer = null;
	static{

		//Register this with mbean server
		ConManMBean csMbean = new ConMan();
		try {
			Deployer.mbs.registerMBean(csMbean, new ObjectName("RootDeployer:name=Root"));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		String name=UUID.randomUUID().toString();
		if(!isSlave()){
			System.out.println("Starting as Master. Name="+name);
			bindTheDeployer(name,false);
		}else{
			System.out.println("Starting as Slave. Name="+name);
			System.setProperty(PP_DIS_AUTO_REG,"true");
			bindTheDeployer(name,true);
		}
	}

	protected static boolean isSlave() {
		return System.getProperty(DEPLOYMENT_DESC)==null;
	}

	private static void bindTheDeployer(String name,final boolean slave) {
		try {
			deployerImpl = new EPDeployerImpl(name);
			exportedDeployer = (EPDeployer) UnicastRemoteObject
					.exportObject(deployerImpl, 0);
			registerWithDiscovery(slave);
		} catch (Throwable e) {
			asynchronouslyShutDown();
			throw new RuntimeException(e);
		}
	}

	static void registerWithDiscovery(boolean slave) throws RemoteException {
		Binder binder = RegistryHelper.getBinder();
		if(slave){
			binder.bindSlave(exportedDeployer);
		}else{
			binder.bindApp(exportedDeployer);						
		}
	}


	public static void deploy(String fileName) throws JAXBException, InterruptedException, ExecutionException {
		System.out.println("Attempting to deploy "+fileName);
		Context context = buildContext(fileName);
		Future<?> future = deploy(context,false);
		future.get();
		System.out.println(fileName+" deployed.");
	}
	
	
	public static Context buildContext(String fileName) throws JAXBException{
		Context context = null;
		InputStream ins = null;
		
		try{
			File file = new File(fileName);
			if(file.exists()){
				ins = new FileInputStream(file);
			}else{
				ins = ClassLoader.getSystemResourceAsStream(fileName);
			}
			context = buildContext(ins);
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
		return context; 
	}


	@SuppressWarnings("unchecked")
	public static Context buildContext(InputStream ins)
			throws JAXBException, SAXException {
		Context context;
		JAXBContext jc = JAXBContext
				.newInstance("com.biswa.ep.deployment.util");

		Unmarshaller unmarshaller = jc.createUnmarshaller();
		JAXBElement<Context> rootObj = (JAXBElement<Context>) unmarshaller
				.unmarshal(getSource(ins));
		context = rootObj.getValue();
		return context;
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
	
	public static Source getSource(InputStream ins) throws SAXException{
		XMLReader reader = XMLReaderFactory.createXMLReader();
		reader.setEntityResolver(new EntityResolver() {
			@Override
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				StringBuilder sb = new StringBuilder();
				ResourceBundle rb = null;
				try{
					rb = ResourceBundle.getBundle("ep");
					Enumeration<String> keys=rb.getKeys();
					while(keys.hasMoreElements()){
						String key = keys.nextElement();
						sb.append("<!ENTITY ");
						sb.append(key);
						sb.append(" '");
						sb.append(rb.getString(key));
						sb.append("'>");
					}
				}catch(MissingResourceException mre){
					System.err.println("Could not locate ep.properties can not resolve entities...");
				}
				InputSource ins = new InputSource(new ByteArrayInputStream(sb.toString().getBytes()));
				return ins;
			}
		});
		Source source = new SAXSource(reader,new InputSource(ins));
		return source;
	}
	
	public static String getName(){
		if(deployerImpl!=null){
			try {
				return deployerImpl.getName();
			} catch (RemoteException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Process peer death asynchronously.
	 * @param name String 
	 * @param deadContainers Collection<String>
	 */
	public static void peerDied(final String name, final Collection<String> deadContainers) {
		deployer.execute(new Runnable(){
			@Override
			public void run(){
				containerManager.peerDied(name, deadContainers);
			}
		});
	}
	
	/**
	 * Asynchronously shuts down the virtual machine.
	 */
	public static void asynchronouslyShutDown() {
		deployer.execute(new Runnable() {			
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				} finally{
					System.exit(0);
				}
			}
		});
	}

	
	public static void main(String[] args) throws JAXBException, InterruptedException, ExecutionException{
		boolean started = false;
		String fileName = System.getProperty(DEPLOYMENT_DESC);
		if(fileName!=null){
			try{
				deploy(fileName);
				started = true;
			}finally{
				if(!started){
					asynchronouslyShutDown();
				}
			}
		}
	}
}
