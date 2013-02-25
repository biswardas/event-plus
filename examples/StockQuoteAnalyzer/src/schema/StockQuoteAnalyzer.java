package schema;

import processor.Quote;

import com.biswa.ep.annotations.EPAttrType;
import com.biswa.ep.annotations.EPAttribute;
import com.biswa.ep.annotations.EPConType;
import com.biswa.ep.annotations.EPContainer;
import com.biswa.ep.annotations.EPContext;
import com.biswa.ep.annotations.EPPublish;

@EPContext()
public interface StockQuoteAnalyzer {
	@EPContainer(type = EPConType.Subscription)
	public interface NYSE {
		@EPAttribute(type = EPAttrType.SubProcessor, processor = "processor.NYSEMarketData")
		Double marketData = null; 
	}

	@EPContainer(type = EPConType.Subscription)
	public interface NASDAQ {
		@EPAttribute(type = EPAttrType.SubProcessor, processor = "processor.NASDAQMarketData")
		Double marketData = null;
	}

	@EPContainer(generator = "generator.Portfolio")
	interface InputStocks {
		String symbol = null;
		Double quantity = null;
		Double tranPrice = null;
	}

	@EPContainer(publish=EPPublish.RMI)
	public interface Portfolio extends InputStocks {
		@EPAttribute(type = EPAttrType.Subscriber, depends = "symbol", container = "NYSE")
		public Quote nysePrice = null;
		
		@EPAttribute(type = EPAttrType.Subscriber, depends = "symbol", container = "NASDAQ")
		public Quote nasdaqPrice = null;
		
		public Double bestBid=nysePrice.bid>nasdaqPrice.bid?nysePrice.bid:nasdaqPrice.bid;
		public Double bestAsk=nysePrice.ask<nasdaqPrice.ask?nysePrice.ask:nasdaqPrice.ask;
		
		public Double stockWorth = quantity>0.0?quantity*bestBid:quantity*bestAsk;
		
		public Double PNL = stockWorth-quantity*tranPrice; 
	}
}
