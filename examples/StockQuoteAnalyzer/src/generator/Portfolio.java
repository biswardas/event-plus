package generator;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

public class Portfolio {
	public void init(Connection connection) throws SQLException {
		CallableStatement call = connection.prepareCall("call StockQuoteAnalyzer.InputStocks.insert(?)");
		put(call,"IBM",100.0,200.0);
		put(call,"CSCO",500.0,20.0);
		put(call,"NVDA",1000.0,15.0);
		put(call,"ORCL",400.0,34.0);
		put(call,"IBM",100.0,200.0);
		put(call,"CSCO",500.0,20.0);
		put(call,"NVDA",1000.0,15.0);
		put(call,"ORCL",400.0,34.0);
		call.executeBatch();
		connection.commit();
	}
	private void put(CallableStatement call,String stock,Double quantity,Double tranPrice) throws SQLException{
		call.setObject("symbol",stock);
		call.setObject("quantity",quantity);
		call.setObject("tranPrice",tranPrice);
		call.addBatch();
	}
	public void terminate(){}
}
