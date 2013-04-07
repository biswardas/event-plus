package com.biswa.ep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Annotates top level element in event plus framework. This is intercepted 
 * by compiler and sent for code generation.
 * @author Biswa
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EPContext {
	/**
	 * Specify the external contexts needs to be imported into the current context.
	 * @return String[]
	 */
	String[] schemas() default {};
	/**
	 * Specify the java packages need to be imported into the current context.
	 * @return String[]
	 */
	String[] packages() default {};
}
