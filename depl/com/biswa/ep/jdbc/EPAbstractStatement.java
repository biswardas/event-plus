package com.biswa.ep.jdbc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.biswa.ep.entities.Attribute;
import com.biswa.ep.entities.LeafAttribute;
import com.biswa.ep.entities.identity.ConcreteIdentityGenerator;
import com.biswa.ep.entities.identity.IdentityGenerator;
import com.biswa.ep.entities.substance.ObjectSubstance;
import com.biswa.ep.entities.substance.Substance;
import com.biswa.ep.entities.transaction.TransactionEvent;

public abstract class EPAbstractStatement extends StatementAdapter {
	private static final IdentityGenerator idGen = new ConcreteIdentityGenerator();
	private static final Pattern pattern = Pattern
			.compile("(?i)call\\s*(.*?)\\.((insert|update|delete)+)\\s*\\(\\s*\\?\\s*\\)\\s*");

	protected enum Operation {
			INSERT, UPDATE, DELETE
		}

	protected Operation op;
	private Integer identity=null;
	private String sink;
	protected TransactionEvent tranID;
	protected EPAbstractConnection sqlConnection;
	private boolean closed = false;
	protected final Map<Integer, Map<Attribute, Substance>> batch = new LinkedHashMap<Integer, Map<Attribute, Substance>>();
	protected Map<Attribute, Substance> hm = new HashMap<Attribute, Substance>();

	public EPAbstractStatement(String sql) throws SQLException{
		parse(sql);
	}


	@Override
	public void setObject(String parameterName, Object inputObject)
			throws SQLException {
		checkClosed();
		hm.put(new LeafAttribute(parameterName), new ObjectSubstance(
				inputObject));
	}

	@Override
	public void setInt(int index, int value) throws SQLException {
		checkClosed();
		if (index != 1) {
			throw new SQLException("Invalid index access->" + index);
		}
		identity = value;
	}
	
	@Override
	public int[] executeBatch() throws SQLException {
		checkClosed();
		int ids[];
		try {
			tranID = beginTran();
			invokeOnContainer();
			ids = new int[batch.size()];
			int index = 0;
			for (Integer oneKey : batch.keySet()) {
				ids[index++] = oneKey;
			}
		} finally {
			clearBatch();
		}
		return ids;
	}
	
	abstract void invokeOnContainer() throws SQLException;

	TransactionEvent beginTran() throws SQLException {
		return sqlConnection.beginTran(this);
	}


	@Override
	public void addBatch() throws SQLException {
		checkClosed();
		switch (op) {
		case INSERT:
			batch.put(getIdentity(), hm);
			hm = new HashMap<Attribute, Substance>();
			identity = null;
			break;
		case UPDATE:
			batch.put(identity, hm);
			hm = new HashMap<Attribute, Substance>();
			break;
		case DELETE:
			batch.put(identity, null);
			break;
		}
	}
	
	protected void checkClosed() throws SQLException {
		if (closed || sqlConnection.isClosed()) {
			throw new SQLException("Statement already closed");
		}
	}

	protected int getTranId() {
		return tranID == null ? 0 : tranID
		.getTransactionId();
	}

	protected int getIdentity() {
		return identity == null ? idGen.generateIdentity() : identity.intValue();
	}

	protected void parse(String sql) throws SQLException {
		Matcher match = pattern.matcher(sql);
		if (match.matches()) {
			sink=match.group(1);
			op = Operation.valueOf(match.group(2).toUpperCase());
		}else{
			throw new SQLException("Statement Parsing Error");
		}
	}

	protected String getSink() {
		return sink;
	}


	@Override
	public void clearBatch() throws SQLException {
		checkClosed();
		batch.clear();
	}

	@Override
	public void close() throws SQLException {
		closed = true;
	}
}