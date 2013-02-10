package com.biswa.ep.discovery;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.biswa.ep.deployment.Accepter;
import com.biswa.ep.deployment.ContainerManager;
import com.biswa.ep.deployment.util.Feedback;
import com.biswa.ep.deployment.util.Listen;
import com.biswa.ep.entities.ConcreteContainer;

public class RMIAccepterImplTest {
	private static ConcreteContainer sourceContainer=new ConcreteContainer("Source.foo",new Properties());
	private static ConcreteContainer sinkContainer=new ConcreteContainer("Sink.foo",new Properties());
	private static Accepter accepter = new RMIAccepterImpl(new ContainerManager());
	@BeforeClass
	public static void registerContainer(){
		accepter.publish(sourceContainer);
		accepter.publish(sinkContainer);
	}
	@Test
	public void testListen() {
		accepter.listen(new Listen(){
			@Override
			public String getContainer() {
				return "foo";
			}

			@Override
			public String getContext() {
				return "Source";
			}
			
		}, sinkContainer);
	}

	@Test
	public void testReplay() {
		accepter.replay(new Listen(){
			@Override
			public String getContainer() {
				return "foo";
			}

			@Override
			public String getContext() {
				return "Source";
			}
			
		}, sinkContainer);
	}

	@Test
	public void testAddFeedbackSource() {
		accepter.addFeedbackSource(new Feedback(){

			@Override
			public String getContext() {
				return "Sink";
			}

			@Override
			public String getContainer() {
				return "foo";
			}

			@Override
			public String getMethod() {
				return "RMI";
			}

			@Override
			public String getAlias() {
				return "Sink.foo";
			}
			
		}, sinkContainer);
	}

	@Test
	public void testGetSubscriptionAgent() {
		assertNotNull(accepter.getSubscriptionAgent("Source", "foo"));
	}
	@AfterClass
	public static void unpublishContainer(){
		accepter.unpublish(sourceContainer);
		accepter.unpublish(sinkContainer);
	}
}
