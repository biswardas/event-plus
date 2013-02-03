package com.biswa.ep.entities.spec;

import java.io.Serializable;

import com.biswa.ep.entities.ContainerListener;

public interface Spec extends Serializable{
	public void apply(ContainerListener listener);
}
