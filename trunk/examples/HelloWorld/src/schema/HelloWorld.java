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