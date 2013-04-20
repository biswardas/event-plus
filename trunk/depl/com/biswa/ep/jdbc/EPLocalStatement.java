package com.biswa.ep.jdbc;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerDeleteEvent;
import com.biswa.ep.entities.ContainerInsertEvent;
import com.biswa.ep.entities.ContainerUpdateEvent;
import com.biswa.ep.entities.TransportEntry;
import com.biswa.ep.entities.transaction.Agent;

/**
 * {call Context.Container.insert}<br>
 * {call Context.Container.delete(?)}<br>
 * {call Context.Container.update(?)}<br>
 * 
 * @author Biswa
 * 
 */
public class EPLocalStatement extends EPAbstractStatement {
	Agent agent = null;

	public EPLocalStatement(EPLocalConnection epLocalConnection, String sql)
			throws SQLException {
		super(sql);
		this.sqlConnection = epLocalConnection;
		this.agent = epLocalConnection.agent;
	}

	void invokeOnContainer() throws SQLException {
		switch (op) {
		case DELETE:
			for (Entry<Integer, Map<Attribute, Object>> oneEntry : batch
					.entrySet()) {
				agent.entryRemoved(new ContainerDeleteEvent(getSink(), oneEntry
						.getKey(), getTranId()));
			}
			break;
		case INSERT:
			for (Entry<Integer, Map<Attribute, Object>> oneEntry : batch
					.entrySet()) {
				agent.entryAdded(new ContainerInsertEvent(getSink(),
						new TransportEntry(oneEntry.getKey(), oneEntry
								.getValue()), getTranId()));
			}
			break;
		case UPDATE:
			for (Entry<Integer, Map<Attribute, Object>> oneEntry : batch
					.entrySet()) {
				for (Entry<Attribute, Object> oneUpdate : oneEntry
						.getValue().entrySet()) {
					agent.entryUpdated(new ContainerUpdateEvent(getSink(),
							oneEntry.getKey(), oneUpdate.getKey(), oneUpdate
									.getValue(), getTranId()));
				}
			}
			break;
		}
	}
	protected String getSink() {
		return agent.getName();
	}
}
