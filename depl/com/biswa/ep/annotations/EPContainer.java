package com.biswa.ep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a container in event plus framework. This is required in order to treat 
 * one class as a container. In case it is missing the class will be treated as a simple
 * helper.
 * @author Biswa
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface EPContainer {
	/**
	 * Specifies the type of container. Defaults to <code>EPConType.Basic</code>
	 * @return
	 */
	EPConType type() default EPConType.Basic;

	/**
	 * Specifies the protocol this container understands. Defaults to 
	 * <code>EPPublish.LOCAL</code> in event of a container needs to be used across 
	 * processes specify <code>EPPublish.RMI</code>. All colocated container will talk
	 * locally instead going over wire.
	 * @return EPPublish
	 */
	EPPublish publish() default EPPublish.LOCAL;


	/**
	 * Specifies the external data input for this container. If this container supports 
	 * <code>EPPublish.RMI</code> then JDBC interface can be used to inject inputs into 
	 * the container. This is provided as convenience so local VM can inject data into 
	 * the container by providing a class here.
	 * @return String
	 */
	Class<? extends Object> generator() default Object.class;

	/**
	 * Specifies the source filter of this container. This container does not propagate
	 * anything not matching this filter.
	 * @return String
	 */
	String filter() default "";
	
	/**Inheriting containers by means of this allows some rich features however adds certain
	 * other restrictions. Required for advanced usage.
	 * 
	 * @return EPRef[]
	 */
	EPRef[] ref() default {};

	/**
	 * Required for join container. If not specified is treated as INNER_JOIN.
	 * @return String
	 */
	EPJoinType join() default EPJoinType.INNER_JOIN;

	/**
	 * Inject the assorted properties into the container. See the properties list a
	 * specific container supports.
	 * @return String
	 */
	String[] params() default {};
}
