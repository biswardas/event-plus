package com.biswa.ep.entities.predicate;

import com.biswa.ep.entities.ContainerEntry;

public class True extends Predicate {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3792357731912933288L;

	@Override
	public boolean visit(ContainerEntry containerEntry) {
		return true;
	}
}
