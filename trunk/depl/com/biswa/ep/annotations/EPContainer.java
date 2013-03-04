package com.biswa.ep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface EPContainer {
	EPConType type() default EPConType.Basic;

	EPPublish publish() default EPPublish.LOCAL;
		
	String generator() default "";
	
	String filter() default "";
	
	EPRef[] ref() default {};
	
	EPJoinType join() default EPJoinType.INNER_JOIN;
	
	String[] params() default {};
}
