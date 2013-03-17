package com.biswa.ep.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

public class JDBCDriverTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Class.forName("com.biswa.ep.jdbc.EPDriver");
	}

	@Test(expected=SQLException.class)
	public void testMalFormedURL() throws ClassNotFoundException, SQLException {		
		DriverManager.getConnection("jdbc:AB:rmi://localhost[1098]");
	}
	
	@Test(expected=SQLException.class)
	public void testNonExistentDestination() throws ClassNotFoundException, SQLException {		
		DriverManager.getConnection("jdbc:ep:rmi://localhost[1098]");
	}
	
	@Test(expected=SQLException.class)
	public void testTryInvalidQuery() throws ClassNotFoundException, SQLException {
		
		Connection connection = DriverManager.getConnection("jdbc:ep:rmi://localhost[1099]");
		connection
				.prepareCall("ball EasySub.InputOptions.insert(?)");
	}

	@Test(expected=SQLException.class)
	public void testTryInvalidContainer() throws ClassNotFoundException, SQLException {
		
		Connection connection = DriverManager.getConnection("jdbc:ep:rmi://localhost[1099]");
		connection
				.prepareCall("call A.InputOptions.insert(?)");
	}

	@Test(expected=SQLException.class)
	public void testTryOpAfterClose() throws ClassNotFoundException, SQLException {
		
		Connection connection = DriverManager.getConnection("jdbc:ep:rmi://localhost[1099]");
		connection.close();
		connection
				.prepareCall("call EasySub.InputOptions.insert(?)");
	}
	
	@Test(expected=SQLException.class)
	public void testTryOpAfterStatementClose() throws ClassNotFoundException, SQLException {
		
		Connection connection = DriverManager.getConnection("jdbc:ep:rmi://localhost[1099]");
		CallableStatement call = connection
				.prepareCall("call EasySub.InputOptions.insert(?)");
		call.close();
		call.setObject("symbol", "X");
		call.addBatch();
		call.executeBatch();
	}

	
	@Test
	public void testInsertWithoutTransaction() throws ClassNotFoundException, SQLException {
		
		Connection connection = DriverManager.getConnection("jdbc:ep:rmi://localhost[1099]");
		CallableStatement call = connection
				.prepareCall("call EasySub.InputOptions.insert(?)");
		call.setObject("symbol", "X");
		call.addBatch();
		call.setObject("symbol", "Y");
		call.addBatch();
		call.setObject("symbol", "Z");
		call.addBatch();
		int[] insertedRecordIds= call.executeBatch();
		updateSymbol(connection,insertedRecordIds, new String[]{"X1","Y1","Z1"});
		System.out.println(Arrays.toString(insertedRecordIds));
		delete(connection,insertedRecordIds);
		connection.close();
	}

	@Test
	public void testInsertWithTransaction() throws ClassNotFoundException, SQLException {		
		Connection connection = DriverManager.getConnection("jdbc:ep:rmi://localhost[1099]");
		connection.setAutoCommit(false);
		int[] insertedRecordIds = insertRecords(connection,new String[]{"XX","YY","ZZ"});
		updateSymbol(connection,insertedRecordIds, new String[]{"XX1","YY1","ZZ1"});
		delete(connection,insertedRecordIds);
		connection.commit();
	}
	
	@Test
	public void testUpdate() throws ClassNotFoundException, SQLException {	
		Connection connection = DriverManager.getConnection("jdbc:ep:rmi://localhost[1099]");
		connection.setAutoCommit(false);
		updateSymbol(connection,new int[]{10001},new String[]{"ALPHA"});
		connection.commit();
	}
	
	@Test
	public void testDelete() throws ClassNotFoundException, SQLException {	
		Connection connection = DriverManager.getConnection("jdbc:ep:rmi://localhost[1099]");
		connection.setAutoCommit(false);
		delete(connection,new int[]{10137,10138,10139});
		connection.commit();
	}
	
	@Test
	public void testMultiCommit() throws ClassNotFoundException, SQLException {
		
		Connection connection = DriverManager.getConnection("jdbc:ep:rmi://localhost[1099]");
		connection.setAutoCommit(false);
		CallableStatement call = connection
				.prepareCall("call EasySub.InputOptions.insert(?)");
		call.setObject("symbol", "XXX");
		call.addBatch();
		call.executeBatch();
		connection.commit();
		
		call.setObject("symbol", "YYY");
		call.addBatch();
		call.executeBatch();
		connection.commit();
		
		call.setObject("symbol", "ZZZ");
		call.addBatch();
		call.executeBatch();
		connection.commit();
		
		connection.close();
	}

	protected void delete(Connection connection,int[] ids) throws ClassNotFoundException, SQLException {
		CallableStatement call = connection
				.prepareCall("call EasySub.InputOptions.delete(?)");
		for(int oneId:ids){
			call.setInt(1,oneId);
			call.addBatch();
		}
		int[] deletedRecordIds = call.executeBatch();
		call.close();
		System.out.println(Arrays.toString(deletedRecordIds));
	}

	protected void updateSymbol(Connection connection,int[] ids,String[] newSymbol) throws ClassNotFoundException,
			SQLException {
		CallableStatement call = connection
				.prepareCall("call EasySub.InputOptions.update(?)");
		for(int i=0;i<ids.length;i++){
			call.setInt(1,ids[i]);
			call.setObject("symbol", newSymbol[i]);
			call.addBatch();
		}
		int[] updatedRecordIds = call.executeBatch();
		System.out.println(Arrays.toString(updatedRecordIds));
		call.close();
	}

	protected int[] insertRecords(Connection connection,String[] symbols) throws ClassNotFoundException, SQLException {
		CallableStatement call = connection
				.prepareCall("call EasySub.InputOptions.insert(?)");
		for(int i=0;i<symbols.length;i++){
			call.setObject("symbol", symbols[i]);
			call.addBatch();
		}
		int[] insertedRecordIds= call.executeBatch();
		System.out.println(Arrays.toString(insertedRecordIds));
		call.close();
		return insertedRecordIds;
	}

}
