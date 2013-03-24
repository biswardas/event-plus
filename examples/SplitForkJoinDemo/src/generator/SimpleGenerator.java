package generator;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class SimpleGenerator {
	public void init(final Connection connection) throws SQLException {
		connection.setAutoCommit(false);
		CallableStatement call = connection
				.prepareCall("call EasySub.InputOptions.insert(?)");
		call.setObject("symbol","IBM");
		call.addBatch();
		call.setObject("symbol","CSCO");
		call.addBatch();
		call.setObject("symbol","NVDA");
		call.addBatch();
		call.setObject("symbol","ORCL");
		call.addBatch();
		call.setObject("symbol","C");
		call.addBatch();
		call.setObject("symbol","BAC");
		call.addBatch();
		call.executeBatch();
		connection.commit();
		connection.close();
	}
	public void terminate(){
	}
}
