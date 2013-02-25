package processor;


public class NYSEMarketData extends MarketData{

	@Override
	protected int updateDelay() {
		return 1;
	}
}
