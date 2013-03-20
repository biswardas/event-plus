package generator;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class YourWorld {
        public void init(final Connection connection) throws SQLException {
                CallableStatement call = connection
                                .prepareCall("call HelloWorld.SayHello.insert(?)");
                call.setObject("name", "John");
                call.addBatch();
                call.executeBatch();
        }
        public void terminate(){                
        }
}