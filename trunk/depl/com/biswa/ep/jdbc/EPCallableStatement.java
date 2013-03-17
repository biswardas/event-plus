package com.biswa.ep.jdbc;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import com.biswa.ep.discovery.RMIListener;
import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.ContainerDeleteEvent;
import com.biswa.ep.entities.ContainerInsertEvent;
import com.biswa.ep.entities.ContainerUpdateEvent;
import com.biswa.ep.entities.TransportEntry;
import com.biswa.ep.entities.substance.Substance;

/**
 * {call Context.Container.insert}<br>
 * {call Context.Container.delete(?)}<br>
 * {call Context.Container.update(?)}<br>
 * 
 * @author Biswa
 * 
 */
public class EPCallableStatement extends EPAbstractStatement {
	private RMIListener rmil = null;

	public EPCallableStatement(EPConnection sqlConnection, String sql)
			throws SQLException {
		super(sql);
		this.sqlConnection = sqlConnection;
		try {
			rmil = (RMIListener) sqlConnection.getRegistry().lookup(getSink());
		} catch (Throwable th) {
			throw new SQLException(th);
		}
	}



	void invokeOnContainer() throws SQLException {
		try {
			switch (op) {
			case DELETE:
				for (Entry<Integer, Map<Attribute, Substance>> oneEntry : batch
						.entrySet()) {
					getRmil().entryRemoved(
							new ContainerDeleteEvent(getSink(), oneEntry
									.getKey(), getTranId()));
				}
				break;
			case INSERT:
				for (Entry<Integer, Map<Attribute, Substance>> oneEntry : batch
						.entrySet()) {
					getRmil().entryAdded(
							new ContainerInsertEvent(getSink(),
									new TransportEntry(oneEntry.getKey(),
											oneEntry.getValue()), getTranId()));
				}
				break;
			case UPDATE:
				for (Entry<Integer, Map<Attribute, Substance>> oneEntry : batch
						.entrySet()) {
					for (Entry<Attribute, Substance> oneUpdate : oneEntry
							.getValue().entrySet()) {
						getRmil().entryUpdated(
								new ContainerUpdateEvent(getSink(), oneEntry
										.getKey(), oneUpdate.getKey(),
										oneUpdate.getValue(), getTranId()));
					}
				}
				break;
			}
		} catch (RemoteException e) {
			throw new SQLException(e);
		}
	}

	RMIListener getRmil() {
		return rmil;
	}
}
