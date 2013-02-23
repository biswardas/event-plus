package com.biswa.ep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface EPContainer {
	EPConType type() default EPConType.Simple;

	String publish() default "";

	Feedback[] feedback() default {};

	public @interface Feedback {
		String context();

		String container();
		
		String publish();

		String alias() default "";		
	}
	
	Transaction[] transaction() default {}; 
	
	public @interface Transaction {
		String[] group() default {};
	}

	String[] params() default {};
}
