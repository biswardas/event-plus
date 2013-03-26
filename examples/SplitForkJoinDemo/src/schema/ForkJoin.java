package schema;

import com.biswa.ep.annotations.EPAttrType;
import com.biswa.ep.annotations.EPAttribute;
import com.biswa.ep.annotations.EPConType;
import com.biswa.ep.annotations.EPContainer;
import com.biswa.ep.annotations.EPContext;
import com.biswa.ep.annotations.EPPublish;
import com.biswa.ep.util.InlineViewer;
import com.biswa.ep.deployment.Deployer;

@EPContext()
public interface ForkJoin {
	@EPContainer(publish=EPPublish.RMI,type=EPConType.Split,generator = "generator.SimpleGenerator")
	interface InputStocks {
		String symbol = null;
	}
	
	@EPContainer(publish=EPPublish.RMI,type = EPConType.Subscription)
	public interface Reuters {
		@EPAttribute(type = EPAttrType.SubProcessor, processor = "processor.FastProcessor")
		Double marketData = null;
	}

	@EPContainer(type=EPConType.ForkJoin,params="ep.slave.count=4")
	public interface InputOptions extends InputStocks{
		@EPAttribute(type=EPAttrType.Subscriber,depends="symbol",container="Reuters")
		public double stockPrice = 0.0;
		public String optionSymbol = symbol + "-March";
		@EPAttribute(type=EPAttrType.Subscriber,depends="optionSymbol",container="Reuters")
		public double optionPrice = 0.0;
		
		public String executingOnSlave=Deployer.getName();
	}
	@EPContainer()
	public interface Viewer extends InputOptions{
		@EPAttribute(type=EPAttrType.Static)
		int launchViewer=InlineViewer.launchViewer();
	}
}
