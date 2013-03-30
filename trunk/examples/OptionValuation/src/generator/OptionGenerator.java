package generator;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.jquantlib.instruments.Option;
import org.jquantlib.time.Date;
import org.jquantlib.time.Month;

public class OptionGenerator {
	public void init(final Connection connection) throws SQLException {
		connection.setAutoCommit(false);
		CallableStatement call = connection
				.prepareCall("call EasySub.InputOptions.insert(?)");
		addInstrument(call, "IBM", new Date(17, Month.May, 1998), new Date(17,
				Month.May, 1999), 40.0, 36.0, Option.Type.Put);
		addInstrument(call, "IBM", new Date(17, Month.May, 1998), new Date(17,
				Month.May, 1999), 40.0, 36.0, Option.Type.Call);
		addInstrument(call, "IBM", new Date(17, Month.May, 1998), new Date(17,
				Month.May, 1999), 35.0, 36.0, Option.Type.Put);
		addInstrument(call, "IBM", new Date(17, Month.May, 1998), new Date(17,
				Month.May, 1999), 35.0, 36.0, Option.Type.Call);
		addInstrument(call, "IBM", new Date(17, Month.May, 1998), new Date(17,
				Month.May, 1999), 45.0, 36.0, Option.Type.Put);
		addInstrument(call, "IBM", new Date(17, Month.May, 1998), new Date(17,
				Month.May, 1999), 45.0, 36.0, Option.Type.Call);
		call.executeBatch();
		connection.commit();
		connection.close();
	}

	protected void addInstrument(CallableStatement call, String symbol,
			Date settlementDate, Date maturity, double strike,
			double underlying, Option.Type type) throws SQLException {
		call.setObject("symbol", symbol);
		call.setObject("settlementDate", settlementDate);
		call.setObject("maturity", maturity);
		call.setObject("strike", strike);
		call.setObject("underlying", underlying);
		call.setObject("type", type);
		call.addBatch();
	}

	public void terminate() {
	}
}
