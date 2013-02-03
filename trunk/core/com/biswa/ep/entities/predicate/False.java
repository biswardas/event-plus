package com.biswa.ep.entities.predicate;

import com.biswa.ep.entities.ContainerEntry;

public class False extends Predicate {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1269939795023812093L;

	@Override
	public boolean visit(ContainerEntry containerEntry) {
		return false;
	}
}
