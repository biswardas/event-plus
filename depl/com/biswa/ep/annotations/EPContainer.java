package com.biswa.ep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.biswa.ep.deployment.handler.ContainerDeployer;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface EPContainer {
	ContainerDeployer type() default ContainerDeployer.Simple;

	String name() default "";

	Feedback[] feedback() default {};

	String publish() default "";
	
	Transaction[] transaction() default {}; 

	public @interface Feedback {
		String context();

		String container();
		
		String publish();

		String alias() default "";		
	}
	
	public @interface Transaction {
		String[] group() default {};
	}
}
