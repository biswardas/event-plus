package com.biswa.ep.entities;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import com.biswa.ep.entities.substance.DecimalSubstance;
import com.biswa.ep.entities.substance.Substance;

public class LeafAttributeTest{
	Attribute attribute = null;
	DecimalSubstance ds;
	public LeafAttributeTest() {
		ds = new DecimalSubstance(5d);
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
			protected Substance evaluate(Attribute attribute,
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
			protected Substance evaluate(Attribute attribute,
					ContainerEntry containerEntry) {
				return null;
			}
			
		});		
		assertNotSame(attribute,null);
		assertNotSame(attribute,new Object());
		assertTrue(attribute.equals(new Attribute("Root"){
			@Override
			protected Substance evaluate(Attribute attribute,
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
