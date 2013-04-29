package com.biswa.ep.util;

import javax.swing.SwingUtilities;

import com.biswa.ep.ContainerContext;
import com.biswa.ep.entities.AbstractContainer;
import com.biswa.ep.entities.ConnectionEvent;

public class InlineViewer {
	public static int launchViewer() {
		final AbstractContainer cs = ContainerContext.CONTAINER.get();
		final String name = cs.getName() + "-Viewer";
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				GenericViewer viewer = new GenericViewer(name) {
					@Override
					public void disconnect(ConnectionEvent connectionEvent) {
						cs.agent().disconnect(
								new ConnectionEvent(cs.getName(), name));
					}
				};
				viewer.setSourceAgent(cs.agent());
				cs.agent()
						.connect(
								new ConnectionEvent(cs.getName(), name, viewer
										.agent()));
			}
		});
		return 0;
	}
}
