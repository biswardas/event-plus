package com.biswa.ep.entities;
/**The Task is the wrapper task from the external world. This task would in some form encompass
 * the Container Task which will be executed on the underlying container.
 * @author biswa
 *
 */
abstract public class OuterTask implements Runnable {
	@Override
	final public void run(){
		try{
			runouter();
		}catch(Throwable th){
			th.printStackTrace();
		}
	}
	abstract protected void runouter();
}
