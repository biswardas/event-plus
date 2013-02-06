package com.biswa.ep.entities;

import java.util.Properties;


public class CopyContainerSchema extends ConcreteContainer{
	public CopyContainerSchema(CascadeContainer ... cascadeSchemas){
		super("Copier-"+buildName(cascadeSchemas),new Properties());
		for(CascadeContainer cascadeSchema:cascadeSchemas){
			cascadeSchema.agent().connect(new ConnectionEvent(cascadeSchema.getName(),getName(),this.agent()));
		}
	}

	static String buildName(CascadeContainer[] cascadeSchemas){
		StringBuffer sb = new StringBuffer();
		for(CascadeContainer cascadeSchema:cascadeSchemas){
			sb.append("-");
			sb.append(cascadeSchema.getName());
			sb.append("-");
		}
		return sb.toString();
	}
}