package processor;

import java.io.Serializable;

public class Quote implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5852499708180939954L;
	final public Double bid;
	final public Double ask;
	public Quote(Double bid,Double ask){
		this.bid=bid;
		this.ask=ask;
	}
	@Override
	public String toString() {
		return "Quote [bid=" + bid + ", ask=" + ask + "]";
	}
}
