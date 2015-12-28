# Stock Quote Analyzer #

## Step 1 Compile ##
Lets win half the battle by running through the use case below. Below example analyzes quotes from two demo stock exchanges and merges the quotes to figure out best bid and ask and computes PNL in realtime.
Below code is in lined from [StockQuoteAnalyzer](http://event-plus.googlecode.com/svn/trunk/examples/StockQuoteAnalyzer).
So we begin by coding few interfaces as simple as below.

```
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
		Quote marketData = null; 
	}

	@EPContainer(type = EPConType.Subscription)
	public interface NASDAQ {
		@EPAttribute(type = EPAttrType.SubProcessor, processor = "processor.NASDAQMarketData")
		Quote marketData = null;
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

```

You must have noticed special annotations @EPContext and @EPContainer around the interfaces right? Yes that's where the magic begins. As we compile this interface just like as if any other standard java program Java compiler will generate special code wiring all the dependencies and deployment descriptors required to forward propagate the dependencies.

During compilation phase Java compiler will delegate all java files annotated with @EPContext to event plus library for special purpose processing and will go through further code generation and sub sequential compilation phases behind the curtain. During compilation compiler will also generate deployment descriptors to deploy the generated code. Below is the deployment descriptor generated post compilation phase.

```xml

```
<?xml version='1.0'?>
<Context name='StockQuoteAnalyzer' xmlns='http://code.google.com/p/event-plus'>
	<Container type='Subscription' name='NYSE'>
		<Publish method='LOCAL' />
		<Attribute className='epimpl.schema.StockQuoteAnalyzer$NYSE$marketData' />
	</Container>
	<Container type='Subscription' name='NASDAQ'>
		<Publish method='LOCAL' />
		<Attribute className='epimpl.schema.StockQuoteAnalyzer$NASDAQ$marketData' />
	</Container>
	<Container type='Basic' name='InputStocks'>
		<Publish method='LOCAL' />
		<Attribute className='epimpl.schema.StockQuoteAnalyzer$InputStocks$symbol' />
		<Attribute className='epimpl.schema.StockQuoteAnalyzer$InputStocks$quantity' />
		<Attribute className='epimpl.schema.StockQuoteAnalyzer$InputStocks$tranPrice' />
		<Source className='epimpl.schema.StockQuoteAnalyzer$InputStocks$Inlet' />
	</Container>
	<Container type='Basic' name='Portfolio'>
		<Publish method='RMI' />
		<Listen container='InputStocks' context='StockQuoteAnalyzer' method='LOCAL' />
		<Attribute className='epimpl.schema.StockQuoteAnalyzer$Portfolio$nysePrice' />
		<Listen container='NYSE' context='StockQuoteAnalyzer' method='LOCAL' />
		<Feedback container='NYSE' context='StockQuoteAnalyzer' method='LOCAL' />
		<Subscribe container='NYSE' context='StockQuoteAnalyzer' method='LOCAL' depends='symbol' response='nysePrice' />
		<Attribute className='epimpl.schema.StockQuoteAnalyzer$Portfolio$nasdaqPrice' />
		<Listen container='NASDAQ' context='StockQuoteAnalyzer' method='LOCAL' />
		<Feedback container='NASDAQ' context='StockQuoteAnalyzer' method='LOCAL' />
		<Subscribe container='NASDAQ' context='StockQuoteAnalyzer' method='LOCAL' depends='symbol' response='nasdaqPrice' />
		<Attribute className='epimpl.schema.StockQuoteAnalyzer$Portfolio$bestBid' />
		<Attribute className='epimpl.schema.StockQuoteAnalyzer$Portfolio$bestAsk' />
		<Attribute className='epimpl.schema.StockQuoteAnalyzer$Portfolio$stockWorth' />
		<Attribute className='epimpl.schema.StockQuoteAnalyzer$Portfolio$PNL' />
	</Container>
</Context>

```
```


Check out event plus package from [Event Plus](http://event-plus.googlecode.com/svn/trunk/) to your preferred directory and build with
[Ant](http://ant.apache.org/). We are compiling the event plus binary first (this will be once) followed by the example here.

```
C:\Users\Biswa\workspace\EventPlus>ant
Buildfile: C:\Users\Biswa\workspace\EventPlus\build.xml

clean:
   [delete] Deleting directory C:\Users\Biswa\workspace\EventPlus\build\test
   [delete] Deleting directory C:\Users\Biswa\workspace\EventPlus\build\sources
   [delete] Deleting directory C:\Users\Biswa\workspace\EventPlus\dist
    [mkdir] Created dir: C:\Users\Biswa\workspace\EventPlus\build\test
    [mkdir] Created dir: C:\Users\Biswa\workspace\EventPlus\build\sources
    [mkdir] Created dir: C:\Users\Biswa\workspace\EventPlus\dist

download-dependency:
      [get] Getting: http://repo1.maven.org/maven2/pcj/pcj/1.2/pcj-1.2.jar
      [get] To: C:\Users\Biswa\workspace\EventPlus\lib\pcj-1.2.jar
      [get] Not modified - so not downloaded
      [get] Getting: http://repo1.maven.org/maven2/junit/junit/4.11/junit-4.11.jar
      [get] To: C:\Users\Biswa\workspace\EventPlus\lib\junit-4.11.jar
      [get] Not modified - so not downloaded

generate:
     [java] parsing a schema...
     [java] compiling a schema...
     [java] com\biswa\ep\deployment\util\Attribute.java
     [java] com\biswa\ep\deployment\util\Container.java
     [java] com\biswa\ep\deployment\util\Context.java
     [java] com\biswa\ep\deployment\util\Feedback.java
     [java] com\biswa\ep\deployment\util\Filter.java
     [java] com\biswa\ep\deployment\util\Handler.java
     [java] com\biswa\ep\deployment\util\JoinPolicy.java
     [java] com\biswa\ep\deployment\util\Listen.java
     [java] com\biswa\ep\deployment\util\ObjectFactory.java
     [java] com\biswa\ep\deployment\util\Param.java
     [java] com\biswa\ep\deployment\util\Pivot.java
     [java] com\biswa\ep\deployment\util\Publish.java
     [java] com\biswa\ep\deployment\util\Sort.java
     [java] com\biswa\ep\deployment\util\Source.java
     [java] com\biswa\ep\deployment\util\Subscribe.java
     [java] com\biswa\ep\deployment\util\Summary.java
     [java] com\biswa\ep\deployment\util\package-info.java

compile:
    [javac] Compiling 247 source files to C:\Users\Biswa\workspace\EventPlus\build\sources

build:
      [jar] Building jar: C:\Users\Biswa\workspace\EventPlus\dist\ep.jar

BUILD SUCCESSFUL
Total time: 3 seconds

C:\Users\Biswa\workspace\EventPlus>cd examples\StockQuoteAnalyzer

C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer>ant
Buildfile: C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer\build.xml

clean:
   [delete] Deleting directory C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer\build\sources
   [delete] Deleting directory C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer\dist
    [mkdir] Created dir: C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer\build\sources
    [mkdir] Created dir: C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer\dist

compile:
    [javac] Compiling 6 source files to C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer\build\sources
    [javac] Invoking on:StockQuoteAnalyzer
    [javac] Processing Completed on:StockQuoteAnalyzer

build:
      [jar] Building jar: C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer\dist\stk_quote.jar

BUILD SUCCESSFUL
Total time: 0 seconds

C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer>
```



## Step 2 Deploy the Application ##
**Start the Server**
```
C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer>start ant run-server
Buildfile: C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer\build.xml

run-server:
     [java] Attempting to deploy StockQuoteAnalyzer.xml
     [java] StockQuoteAnalyzer.xml deployed.
     [java] #############PPL-StockQuoteAnalyzer.NASDAQ-2:EPANONYMOUS origins[]
     [java] #############PPL-StockQuoteAnalyzer.InputStocks-3:EPANONYMOUS origins[]
     [java] #############PPL-StockQuoteAnalyzer.NYSE-4:EPANONYMOUS origins[]
     [java] Registering:ContainerSchema:name=StockQuoteAnalyzer.Portfolio
     [java] #############PPL-StockQuoteAnalyzer.Portfolio-5:StockQuoteAnalyzer.InputStocks origins[StockQuoteAnalyzer.InputStocks,
 EPANONYMOUS]
     [java] #############PPL-StockQuoteAnalyzer.Portfolio-5:StockQuoteAnalyzer.NASDAQ origins[StockQuoteAnalyzer.NASDAQ, EPANONYMO
US]
     [java] #############PPL-StockQuoteAnalyzer.Portfolio-5:StockQuoteAnalyzer.NYSE origins[StockQuoteAnalyzer.NYSE, EPANONYMOUS]
     [java] #############PPL-Viewer-StockQuoteAnalyzer.Portfolio (Fri Mar 01 23:35:46 EST 2013)-Stub-10:StockQuoteAnalyzer.Portfol
io origins[StockQuoteAnalyzer.NYSE, StockQuoteAnalyzer.Portfolio, StockQuoteAnalyzer.InputStocks, StockQuoteAnalyzer.NASDAQ, EPANO
NYMOUS]
C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer>
```

**Start the demo Swing Client**
```
C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer>start ant run-client
Buildfile: C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer\build.xml

run-client:
     [java] #############PPL-Viewer-StockQuoteAnalyzer.Portfolio (Fri Mar 01 23:45:52 EST 2013)-1:StockQuoteAnalyzer.Portfolio ori
gins[StockQuoteAnalyzer.NYSE, StockQuoteAnalyzer.Portfolio, StockQuoteAnalyzer.InputStocks, StockQuoteAnalyzer.NASDAQ, EPANONYMOUS
]
C:\Users\Biswa\workspace\EventPlus\examples\StockQuoteAnalyzer>
```
![http://event-plus.googlecode.com/svn/trunk/examples/StockQuoteAnalyzer/epclient.png](http://event-plus.googlecode.com/svn/trunk/examples/StockQuoteAnalyzer/epclient.png)

**Congratulations** Now its time to get hands dirty, learn [EventPlus](EPLanguage.md) here.