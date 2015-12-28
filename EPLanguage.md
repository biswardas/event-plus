# `EventPlus` Language #

**`EventPlus`** framework can be divided into 3 components. **Language** (just a bit restrictive Java), **[Compiler](EPCompiler.md)**, and **[Runtime](EPRuntime.md)**. We can code everything by hand and deploy it in `EventPlus` run-time. Just like a java class can be manufactured from thin air bypassing entire Java Language and Compiler and run in Java run-time its that simple and easy.
We will cover all 3 sections below however as a event plus user we will spend most of our time in the language section. `EventPlus` language is just plain old java with some special purpose annotations as discussed here.

### Context ###
  * **`@EPContext`** **Required:** YES

Top level element in Event Plus is an interface. It defines one technical / business module. Not necessarily has to be the complete business solution. Remember multiple Context can interact with each other for parallel development to build the entire solution. Context provides a name space to the module. In analogy to hardware think it like a pci card hosting interconnected chips. All the java classes annotated with `@EPContext` goes through special code generation phase during compilation.



```

@EPContext()
public interface StockQuoteAnalyzer {
}

```


Below is one advanced component taking advantage of processing already done in component above. See the `schemas` attribute depicitng its dependency on `StockQuoteAnalyzer` context.
```

@EPContext(schemas={"StockQuoteAnalyzer"})
public interface AdvancedQuoteAnalyzer {
}

```
### Container ###

  * **`@EPContainer`** **Required:** YES

In above hardware analogy one container is a chip on a pci card. Does specific processing as programmed to do so. In the example below `InputStocks` is a container which takes input from a hand coded java program `generator.Portfolio`. The container below can also be written as `class InputStocks` to get hold of more features(will be discussed later).

```
@EPContext()
public interface StockQuoteAnalyzer {
        ................
        ................
        ................

        @EPContainer(generator = "generator.Portfolio")
        interface InputStocks {
                String symbol = null;
                Double quantity = null;
                Double tranPrice = null;
        }
        ................
        ................
        ................

```



  * **Relationships**
    1. **Inheritance** One container inherits all attributes and its entries as specified by extends and/or implements.
```
	@EPContext
	public interface QuickCheck {
		@EPContainer(generator="generator.YourWorld")
		public class Helper {
	        	public Double x=null;
	        	public Double y=null;
		}
		@EPContainer
		public class Calculator extends Helper{
			public Double multiply(Double x, Double y) {
				return x * y;
			}
		}
	}
```
    1. **Union** Container merges all attributes and values both vertically and horizontally. Let take example below. Container AB inherits both A and B. AB has now 4 attributes `a1,a2,b1,b2` and assume A has 4 entries and B has 5 entries then AB will have 9 entries all together. Value of attributes from other containers will be defaulted in AB container. **If container A and B hosting entries with same identity then records will be merged ending with only 5 entries.** Mode of union can be configured to Substitute(default),Merge,Ignore.
```
	@EPContext
	public interface UnionDemo {
		@EPContainer(generator="generator.InputA")
		public interface A{
	        	public Double a1=null;
	        	public Double a2=null;
		}
		@EPContainer(generator="generator.InputB")
		public interface B{
	        	public Double b1=null;
	        	public Double b2=null;
		}
		@EPContainer
		public class AB implements A,B{
		}
	}
```
    1. **Join** Relational Join allows to join only 2 containers on certain key. Merge mode Union is a very restrictive form of Join  and can join more than 2 containers. Its recommended to use Union in place of Join where ever possible.
    1. **Subscription** This is a special form of relation ship between containers. Entries of one container create on demand subscription on other container and the server container fulfills the obligation based on the sink container request. If you look at the code snippet below here Portfolio is Sink container requesting to container NYSE through a Subscriber attribute named nysePrice.
```
	@EPContext
	public interface StockQuoteAnalyzer {
		@EPContainer(type = EPConType.Subscription)
		public interface NYSE {
			@EPAttribute(type = EPAttrType.SubProcessor, processor = "processor.NYSEMarketData")
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
			public String[] optionSymbols = {symbol+"-mar",symbol+"-jun",symbol+"-sep",symbol+"-dec"};
			@EPAttribute(type = EPAttrType.Subscriber, depends = "optionSymbols", container = "NYSE")
			public Quote[] optionPrices = null;

                }
        }
```

  * **Container Entry**  Container entry is equivalent to a database record.
    1. **Entry Identity** Each entry in one container has a unique identity which `EventPlus` run-time uses to track the entry during its life cycle. As a developer you need the same id to modify the attribute values or delete the entry from the container.
    1. **Identity Conflicts** In the event of your container subscribes to more that 2 containers hosting conflicting identity sequence then there is a configuration option `resolve.identity.conflicts` to be used to resolve the conflicts. However we will lose our flexibilities to track the record by our designated identifier in the subsequent downstream containers.

  * **Container types**
    1. **Static** Hosts only static attributes. Static attributes do not propagate during inheritance unless defined in a Static container. So this container offers re-usability of static attributes across your solution space.
    1. **Basic** This is the foundation container. Unless otherwise specified this is the default container specification used. Most scenarios this will be container under use.
    1. **Timed** This is a time throttled container and dispatches batched updates as configured `timed.interval`.
    1. **Feedback** As name suggests this is a feedback driven container (flow control) and dispatches updates only after the consuming containers acknowledges to this container for completion of the processing of previous task. This container always generates transactional updates.
    1. **Subscription** is a special purpose feedback container offers subscription services to connecting containers interested in specific piece of information. Connection to the external world is handled through defined `processor`.
    1. **Proxy** is a special purpose container is used as the agent of the subscribers in the sink context. It is used to save network bandwidth and memory where lot of entities in the sink container subscribes very few distinct elements from the source container. i.e Option Series subscribing same underlying price for valuation.
    1. **Join** It offers relational join of 2 entries from 2 different containers based on certain criteria. Performing a true join is extremely expensive its like comparing two flowing water streams. Try to avoid this container whenever other alternatives are available. This will be discussed in detail in run-time section.
    1. **Split** container partition the entries in its downstream container for data parallel processing. Any container connecting is assumed to be a processor node. Do **NOT** connect a viewer to a split container. When a processor node disconnects this container re-balances task among other available processing nodes.
    1. **ForkJoin** This container can only connect to **Split** and Static container and divides work across hardware and aggregate the result back into the host process. This container will try to obtain `ep.slave.count` # of slave process(max 31 allowed will be discussed in run-time section in more detail) to distribute its work. In the event of untimely death of a slave Split will redistribute the work among other available slave processes.![http://event-plus.googlecode.com/svn/trunk/examples/SplitForkJoinDemo/forkjoin.png](http://event-plus.googlecode.com/svn/trunk/examples/SplitForkJoinDemo/forkjoin.png)
    1. **Pivot** This is a presentation layer container and needed to be used with user interfaces requiring to pivot/sort/summarize results. This offers windowing capabilities so you can virtualize your application user interface.


### Attribute ###

  * **`@EPAttribute`** **Required:** NO Default: `@EPAttribute(type=EPAttrType.Member)`

In the analogy to hardware one attribute processes one bit of information entering into the chip through its data bus. Attribute defines the behavior of the the host Container. Unless specified all members in the Containers annotated with @EPContainer are treated as the default Member attribute.

```
@EPContext()
public interface StockQuoteAnalyzer {
        ................
        ................
        ................

        @EPContainer(generator = "generator.Portfolio")
        interface InputStocks {
		@EPAttribute(type=EPAttrType.Member)
                String symbol = null;
		@EPAttribute(type=EPAttrType.Member)
                Double quantity = null;
		@EPAttribute(type=EPAttrType.Member)
                Double tranPrice = null;
        }
        ................
        ................
        ................

```


  * **Attribute typs and Access Privileges**
    1. **Static** Just as `static` keyword in Java Language Static Attributes live in the container memory space not in each Entry. They can drive changes to all entries in the host container in case of dependencies. These attributes in short save memory other wise we would have end up spending some memory for each entry in the container. Static attributes can depend on other static attributes but not to others.
    1. **Member** All attributes unless otherwise specified are treated as member attribute in the container. These attributes flow to down stream containers unless declared with private java keyword. These attributes can depend on other member attributes and static attributes.
    1. **Subscription** These are special purpose member attributes used where some external world information need to be injected into the system based on certain conditions. Such as Stock market prices. These attributes are always dependent on other member attribute but can NOT depend on Static Attribute. These attributes flow to down stream containers unless declared with private java keyword.
    1. **SubProcessor** This is a member attribute only lives in source Subscription Container. This is used in conjunction with **Subscription** attribute in sink side container. These attributes can NOT be declared private.
    1. **Private** Just as `private` keyword in Java Language Private Attributes are only visible in the same container and they do not propagate the information updates to listening containers. This is helpful where you like to mark an attribute as private in an interface. If you are using class keyword to define your container you most likely will not need this.
    1. **Stateless** Stateless attributes live in the space of container entries how ever do not consume any memory. Every time a down stream container requests connection these attributes are recomputed.  Remember to not putting any business logic where you do not wish to perform same activity multiple times like sending an email. **If the container is the terminal container and no one listens to this container these attributes will have no effect.** These attributes flow to down stream containers unless declared with private java keyword.


  * **Attribute dependencies**
If you look at the container below fahrenheit attribute is dependent on celsius  and is re-evaluated whenever celsius changes.
```
	@EPContainer()
	public interface Weather{
		public Double celsius = null;
		public Double fahrenheit= (celsius*9/5)+32;
	}
```
How do I introduce dependencies where exists a self dependency. Let say I want to record min/max temperature recorded.
```
	@EPContainer()
	public interface Weather{
		public Double celsius = null;
		public Double fahrenheit= (celsius*9/5)+32;
                //public Double maxTemprature = maxTemprature > fahrenheit?maxTemprature :fahrenheit; //Compilation failure
	}
```

Remember we spoke about we can use class instead of interface. Now its the time to look into class. Also we need to use class when exists a complex calculation which can not be in lined with a single initialization expression. If you look at the method below we are taking all inputs we care about in the formal arguments. And the name of the formal argument must be same as member argument in order compiler to succeed. Else compilation will fail though you may be seeing it all good in Eclipse. Oh Yes we yet have to talk about [development environment](IDE.md).

```
	@EPContainer()
	public class Weather{
		public Double celsius = null;
		public Double fahrenheit= (Celsius*9/5)+32;
                public Double maxTemprature(Double maxTemprature,Double fahrenheit){
                 if(maxTemprature==null){
                  return fahrenheit;
                 }else{
                  return maxTemprature>fahrenheit?maxTemprature:fahrenheit;
                 }
                }
                public Double minTemprature(Double minTemprature,Double fahrenheit){
                 if(minTemprature==null){
                  return fahrenheit;
                 }else{
                  return minTemprature<fahrenheit?minTemprature:fahrenheit;
                 }
                }
	}
```

All good till now how ever I do not want to rewrite my business logic again its all coded already. Fair enough lets rewrite the example above
with your already existing business code.

```
	@EPContainer()
	public class Weather{
                //Lets mark it static so we do not end up creating converter object for every entry.
                //Thread safety of any helper object if onto you.
                @EPAttribute(type=AttrType.Static)
		public Converted myConverter = new Converter();
		public Double celsius = null;
		public Double fahrenheit= myConverter.conert(celsius);
	}
```
**Can I depend on static attributes?** Lets take a step back Before we talk about this lets understand why do we even care about static attributes. Lets introduce a term called fairly static. Like our federal tax rates. Which do change and when changes nearly applicable to all of us. Lets look at the example below we could have simply created an interface and done with it. Although it works but not optimal and can lead to inconsistency.
```
	@EPContainer()
	public interface ComputeTax{
		public Integer taxRate= 10;
		public Double income= null;
		public Double tax= income*taxRate/100;

	}
```
By defining like below whenever taxRate is changed its applied universally to all entries.
```
	@EPContainer()
	public interface ComputeTax{
		@EPAttribue(type=AttrType.Static)
		public Integer taxRate= 10;
		public Double income= null;
		public Double tax= income*taxRate/100;
	}
```
Cant we have done this? Yes and NO. You can inherit any interfaces/classes as Java language allows without marked with @EPContainer the members and attributes will just behave as helper members and can not propel any forward dependencies. In the sample example below there is no way taxRate can be changed without rebuilding code. OK you can argue "I can code in certain way so that i can change the value of it without rebuilding. Let say changing Integer with AtomicInteger." Still NO yes you can change the value of it but it will just introduce inconsistencies.
```
	public interface Constant{
		public Integer taxRate= 10.0;
	}
	@EPContainer()
	public interface ComputeTax implements Constant{
		public Double income= null;
		public Double tax= income*taxRate/100;
	}
```