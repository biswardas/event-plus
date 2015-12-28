### What is Event Plus? ###
**`EventPlus`** is a real-time event processing, attribution language and dependency management framework to observe and react to any fast moving information. Designed primarily keeping financial market in focus. Typical use cases include risk management, portfolio management & to trigger barrier events. How ever can be used in other industries for alike use cases. It can run inside in any JVM / Application container or run in its standalone JVM and / or distributed across multiple hardware/operating systems. See [case studies](CaseStudies.md) to get an understanding of the framework and its capabilities.
![http://event-plus.googlecode.com/svn/trunk/documentation/portfolio_uc.png](http://event-plus.googlecode.com/svn/trunk/documentation/portfolio_uc.png)

### Features ###

  * Framework is inspired by System On Chip hardware architecture, It has grid solution,messaging,distributed computing all features embedded. Allowing developer to completely focus on business objective.
  * Framework is highly multi-threaded and uses parallel computing whenever its possible for it to do so. This is achieved behind the developers knowledge in-fact developer does not need to directly deal with threads. It offers configuration to control how much computing cores you like to dedicate to certain activities.
  * Framework is also multiprocessing capable and can spawn across hardware to effectively use hardware pool. Every node in the processing graph is independent in nature and can run in a remote process.
  * Framework has transaction & flow control inbuilt and is used to throttle and maintain consistency as required.
  * Your business code remain yours except rare scenarios you don't have to import event plus packages in your code base. That gives you complete liberty to move away without paying any residual penalty for trying this. By concept `EventPlus` encapsulates your business problem and do not get married to your business logic.
  * Highly extensible and offers different plugin points to extend and add special behavior without modifying underlying source code.
  * Not tied to any particular application container or any framework. Can be integrated with any other library you may wish.

### What is not Event Plus? ###
  * This is not a substitute to [Esper](http://esper.codehaus.org/)/[Drools Fusion](http://www.jboss.org/drools/drools-fusion.html) how ever can work with either if your business problem demands such.
  * This is not a GUI program, Though there is a simple viewer packaged in library can be used for debugging.
  * This is not a rule engine, However certain aspects of rule engine are embedded in behavior and it can encapsulate any rule engine you may like to use along with this.
  * I heard [Spring](http://www.springsource.org/) also has dependency stuff, Is this just like that? NO see case studies to get an understanding about this.
  * Is it another language? No However syntactically you can think it like restricted version of Java where the compiler generates special code to achieve your business objective.
  * This is not a database, it ships one `JDBC` driver to interact with your container remotely. The driver is a limited `JDBC` implementation. You can use the event plus api to communicate with containers remotely.



### What do I do to get started? ###

You will just need to write some simple java interfaces defining your business problem and rest is on us. EventPlus will trick the Java compiler to generate code to be deployed. Check the example [here](EventPlus.md).

Check out the Repository [SVN](http://event-plus.googlecode.com/svn/trunk/)

### How does it work? ###

Lets talk about our version of hello world here. As soon name is inserted into the `SayHello` container in `HelloWorld` Context greetings dependency is executed.
```
package schema;

import java.util.Calendar;

import com.biswa.ep.annotations.EPContainer;
import com.biswa.ep.annotations.EPContext;
@EPContext
public interface HelloWorld {
	@EPContainer(generator="generator.YourWorld")
	public class SayHello{
		public String name = null;
		void sayHello(String name){
			Integer AMPM=Calendar.getInstance().get(Calendar.AM_PM);
			if(AMPM==Calendar.AM){
				System.out.println("Good Morning " + name );
			}else{
				System.out.println("Good Afternoon " + name );				
			}
		}
	}
}

```

Your business code to interact with above interface.

```
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
```

### Contributors ###
**Biswa Das** [![](http://www.linkedin.com/img/webpromo/btn_in_20x15.png)](http://www.linkedin.com/pub/biswa-das/7/341/27/)