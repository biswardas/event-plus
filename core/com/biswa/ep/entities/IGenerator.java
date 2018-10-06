package com.biswa.ep.entities;

import java.sql.Connection;
import java.sql.SQLException;
/**
 * Embedded Generator Interface.
 * 
 * @author Biswa
 *
 */
public interface IGenerator {

	void init(Connection connection) throws SQLException;

	void terminate();

}