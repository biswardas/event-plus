package com.biswa.ep.deployment;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.biswa.ep.deployment.util.Context;

public class DeployerTest {
	@Test
	public void testParameterizedArguments() throws SAXException, JAXBException,
			ParserConfigurationException, IOException {
		Source source = Deployer.getSource(ClassLoader.getSystemResourceAsStream("ForkJoin.xml"));
		JAXBContext jc = JAXBContext
				.newInstance("com.biswa.ep.deployment.util");
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		try{
			ResourceBundle rb = ResourceBundle.getBundle("ep");
			JAXBElement<Context> jaxbElement= (JAXBElement<Context>) unmarshaller.unmarshal(source);
			Context context = jaxbElement.getValue();
			Assert.assertEquals(rb.getString("slavecount"),(context.getContainer().get(2).getParam().get(0)).getValue());
		}catch(MissingResourceException me){
			System.err.println("Create BUT DONT CHECK IN THE ep.properties in classpath.");
		}
	}

}
