package com.biswa.ep.util;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ConcreteContainer;
import com.biswa.ep.entities.ConnectionEvent;

public class Viewer extends GenericViewer {
	public Viewer(ConcreteContainer cs,final Attribute ... attr) {
		super(cs.getName());
		start();
		cs.agent().connect(new ConnectionEvent(cs.getName(),getName(),this.agent()));
	}
}