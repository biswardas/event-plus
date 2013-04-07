package com.biswa.ep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface EPRef {
	/**
	 * Name of the context this container is trying to inherit from. Defaults to
	 * current context.
	 * @return String
	 */
	String context() default "";
	/**
	 * Name of the container being inherited.
	 * @return String
	 */
	String container();
	/**
	 * Any filter to apply while inheriting.
	 * @return String
	 */
	String filter() default "";

	/**
	 * Filtering mode this container applies.
	 * @return Mode
	 */
	Mode mode() default Mode.AND;
	/**
	 * All available filtering modes.
	 * @author Biswa
	 *
	 */
	enum Mode{
		AND,OR,NONE;
	}
}
