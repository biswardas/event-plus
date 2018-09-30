package com.biswa.ep.entities;

import java.util.logging.Level;
import java.util.logging.Logger;

/**The Task is the wrapper task from the external world. This task would in some form encompass
 * the Container Task which will be executed on the underlying container.
 * @author biswa
 *
 */
abstract public class OuterTask implements Runnable {
	static final Logger logger = Logger.getLogger(OuterTask.class.getName());
	@Override
	final public void run(){
		try{
			runouter();
		}catch(Throwable th){
			logger.log(Level.WARNING,"Exception occured in thread:"+Thread.currentThread().getName(),th);
		}
	}
	abstract protected void runouter();
}
