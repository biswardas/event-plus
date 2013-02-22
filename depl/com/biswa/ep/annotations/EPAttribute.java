package com.biswa.ep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD, ElementType.TYPE })
public @interface EPAttribute {
	EPAttrType type() default EPAttrType.Member;

	String depends() default "SUBJECT";
	
	String context() default "";
	
	String container() default "";
	
	String response() default "";
	
	String processor() default "";
}