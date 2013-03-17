package com.biswa.ep.jdbc;

import java.sql.SQLException;

import com.biswa.ep.entities.transaction.TransactionEvent;
import com.biswa.ep.entities.transaction.TransactionGenerator;
import com.biswa.ep.entities.transaction.TransactionGeneratorImpl;

public abstract class EPAbstractConnection extends ConnectionAdapter {

	protected static final TransactionGenerator tranGen = new TransactionGeneratorImpl();
	protected boolean closed = false;
	private boolean autoCommit = true;

	public EPAbstractConnection() {
		super();
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		this.autoCommit=autoCommit;
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return autoCommit;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return closed;
	}

	protected void checkClosed() throws SQLException {
		if(isClosed()){
			throw new SQLException("Statement already closed");
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
	abstract TransactionEvent beginTran(EPAbstractStatement statement) throws SQLException;
}