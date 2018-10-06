package com.biswa.ep.jdbc;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.rmi.PortableRemoteObject;

import com.biswa.ep.discovery.Binder;
import com.biswa.ep.discovery.RMIListener;
import com.biswa.ep.entities.transaction.TransactionEvent;

class EPConnection extends EPAbstractConnection implements Connection {
	private static final Pattern pattern = Pattern
			.compile("jdbc:ep:rmi://(.*?)\\[([0-9]*?)\\]");
	private Binder binder;
	final private Properties info;
	private static class TransactionOwner{
		private EPCallableStatement epCallableStatement;
		private TransactionEvent transactionEvent;
		private TransactionOwner(EPCallableStatement epCallableStatement,TransactionEvent transactionEvent){
			this.epCallableStatement=epCallableStatement;
			this.transactionEvent=transactionEvent;
		}
	}
	private Map<String,TransactionOwner> transactionMap = new ConcurrentHashMap<String,TransactionOwner>();
	public EPConnection(String url, Properties info) throws SQLException {
		Matcher matcher = pattern.matcher(url);
		if(matcher.matches()){
			String host=matcher.group(1);
			int port=Integer.parseInt(matcher.group(2));
			try {
				binder = (Binder) PortableRemoteObject.narrow(LocateRegistry.getRegistry(host, port).lookup(Binder.BINDER),Binder.class);
			} catch (RemoteException e) {
				throw new SQLException(e);
			} catch (NotBoundException e) {
				throw new SQLException(e);
			}
		}
		this.info=info;
	}
	
	@Override
	public void commit() throws SQLException {
		boolean flag = true;
		for(TransactionOwner ep:transactionMap.values()){
			try {
				ep.epCallableStatement.getRmil().commitTran(ep.transactionEvent);
				transactionMap.clear();
			} catch (RemoteException e) {
				e.printStackTrace();
				flag=false;
			}
		}
		if(!flag) throw new SQLException("Check log for more errors.");
	}

	@Override
	public void rollback() throws SQLException {
		boolean flag = true;
		for(TransactionOwner ep:transactionMap.values()){
			try {
				ep.epCallableStatement.getRmil().rollbackTran(ep.transactionEvent);
				transactionMap.clear();
			} catch (RemoteException e) {
				e.printStackTrace();
				flag=false;
			}
		}
		if(!flag) throw new SQLException("Check log for more errors.");
	}

	@Override
	public void close() throws SQLException {
		if(!isClosed()){
			rollback();
			closed=true;
			transactionMap.clear();
		}
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		checkClosed();
		EPAbstractStatement epCall = new EPCallableStatement(this,sql);
		return epCall;
	}

	Properties getInfo() {
		return info;
	}
	
	TransactionEvent beginTran(EPAbstractStatement statement) throws SQLException {
		if (!getAutoCommit()) {
			TransactionOwner tranID  = transactionMap.get(statement.getSink());
			if (tranID==null) {
				tranID = new TransactionOwner((EPCallableStatement) statement,new TransactionEvent(statement.getSink(), tranGen.getNextTransactionID()));
				transactionMap.put(statement.getSink(), tranID);
				try {
					((EPCallableStatement)statement).getRmil().beginTran(tranID.transactionEvent);
				} catch (RemoteException e) {
					throw new SQLException(e);
				}
			}
			return tranID.transactionEvent;
		}
		return null;
	}

	RMIListener lookup(String sinkName) throws ClassCastException, RemoteException {
		return (RMIListener) PortableRemoteObject.narrow(binder.lookup(sinkName),RMIListener.class);
	}
}



