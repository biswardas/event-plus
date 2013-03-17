package com.biswa.ep.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import com.biswa.ep.entities.transaction.Agent;
import com.biswa.ep.entities.transaction.TransactionEvent;

public class EPLocalConnection extends EPAbstractConnection implements Connection {
	Agent agent;
	TransactionEvent transactionEvent = null;
	public EPLocalConnection(Agent agent) throws SQLException {
		this.agent=agent;
	}
	
	@Override
	public void commit() throws SQLException {
		if(transactionEvent!=null){
			agent.commitTran(transactionEvent);
			transactionEvent = null;
		}
	}

	@Override
	public void rollback() throws SQLException {
		if(transactionEvent!=null){
			agent.rollbackTran(transactionEvent);
			transactionEvent = null;
		}
	}

	@Override
	public void close() throws SQLException {
		if(!isClosed()){
			rollback();
			closed=true;
			transactionEvent=null;
		}
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		checkClosed();
		EPAbstractStatement epCall = new EPLocalStatement(this,sql);
		return epCall;
	}

	TransactionEvent beginTran(EPAbstractStatement statement) throws SQLException {
		if (!getAutoCommit()) {
			if (transactionEvent==null) {
				transactionEvent = new TransactionEvent(statement.getSink(), tranGen.getNextTransactionID());
				agent.beginTran(transactionEvent);
			}
		}
		return transactionEvent;
	}
}



