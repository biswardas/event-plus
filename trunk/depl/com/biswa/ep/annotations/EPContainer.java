package com.biswa.ep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface EPContainer {
	EPConType type() default EPConType.Simple;

	EPPublish publish() default EPPublish.LOCAL;
	
	String context() default "";
	
	String container() default "";
	
	String generator() default "";

	String[] params() default {};
}
