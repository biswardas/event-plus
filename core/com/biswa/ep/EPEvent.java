package com.biswa.ep;

import java.util.EventObject;



public class EPEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6034865045431637385L;	/**
	 * The name by which the updates are sent to the underlying container.
	 */
	public static final String DEF_SRC = "EPANONYMOUS";

	private String source;

	/**Event Object source is not serializable.
	 * The class maintains source object in form of String so it can be
	 * serialized over network.
	 * @param source
	 */
	public EPEvent(String source) {
		super(source);
		this.source=source;
	}
	
	@Override
	public final String getSource() {
		return source;
	}
}