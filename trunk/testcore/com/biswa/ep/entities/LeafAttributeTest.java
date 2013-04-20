package com.biswa.ep.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

public class LeafAttributeTest{
	Attribute attribute = null; 
	public LeafAttributeTest() { 
		attribute = new LeafAttribute("Root");
	}
	

	@Test
	public void testGetName() {
		assertEquals("Root", attribute.getName());
	}

	@Test
	public void testLeafAttributeAddDependsOn() {
		attribute.addDependency(new Attribute("NewLeafAttribute"){
			@Override
			protected Object evaluate(Attribute attribute,
					ContainerEntry containerEntry) {
				return null;
			}
			
		});
		assertEquals(0, attribute.dependsOn().length);
	}
	@Test
	public void testLeafAttributeEquals() {
		assertEquals(attribute,new Attribute("Root"){
			@Override
			protected Object evaluate(Attribute attribute,
					ContainerEntry containerEntry) {
				return null;
			}
			
		});		
		assertNotSame(attribute,null);
		assertNotSame(attribute,new Object());
		assertTrue(attribute.equals(new Attribute("Root"){
			@Override
			protected Object evaluate(Attribute attribute,
					ContainerEntry containerEntry) {
				return null;
			}
			
		}));
		Assert.assertFalse(attribute.equals(null));
		try{
			attribute.equals(new Object());
			fail("Shouldhave thrown exception");
		}catch(Exception e){
		}
	}
}
