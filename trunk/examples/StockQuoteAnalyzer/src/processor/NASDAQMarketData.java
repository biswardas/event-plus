package processor;


public class NASDAQMarketData extends MarketData{

	@Override
	protected int updateDelay() {
		return 3;
	}
}
