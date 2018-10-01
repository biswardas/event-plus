package com.biswa.ep.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates the member inside a
 * <code>@EPContainer<code>. In absense of the annotation
 * member is treated as <code>EPAttrType.Member</code>. Sample usage below
 * below.
 * <br>
 * <code>
 * <b>Subscribing price from container named $LivePrice for the symbol $stockSymbol</b>
 * @EPAttribute(type = EPAttrType.Subscriber, 
 * depends = "$stockSymbol", 
 * container = "$LivePrice")
 * <code>
 * <br>
 * Special processing on the dependency can be performed here. Let say we like to add suffix 
 * to the symbol then can be done so by providing a special handler class in processor.
 * <code>
 * @EPAttribute(type = EPAttrType.Subscriber, 
 * depends = "$stockSymbol", 
 * container = "$LivePrice",
 * context = "$MyPriceProvider",
 * processor="mypackage.SymbolPrefixAdder")
 * <code>
 * <br>
 * <code>
 * @EPAttribute(type = EPAttrType.SubProcessor, 
 * processor="mypackage.ExternalPriceConnector")
 * <code>
 * 
 * <code>
 * @EPAttribute(type = EPAttrType.Static) This is maintained per container and do not propagate unless defined in a static container.
 * @EPAttribute(type = EPAttrType.Private) This do not propagate to listening containers.
 * @EPAttribute(type = EPAttrType.Member) This is the default unless specified otherwise.
 * @EPAttribute(type = EPAttrType.Stateless) This is transient in nature and is not preserved in the container.
 * <code>
 * @author Biswa
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
public @interface EPAttribute {
	/**
	 * Specifies the type of the
	 * <code>EPAttrType<code>. If this attribute is missing
	 * treated as <code>EPAttrType.Member</code>.
	 * 
	 * @return EPAttrType
	 */
	EPAttrType type() default EPAttrType.Member;

	/**
	 * Specify the attribute name this attribute depends on. It is required for
	 * <code>EPAttrType.Subscriber</code>. If not specified this will inject the
	 * dependency attribute by default.
	 * 
	 * @return String
	 */
	String depends() default "SUBJECT";

	/**
	 * Specify the context name this attribute is subscribing from. If not
	 * specified it will default to local context.
	 * 
	 * @return String
	 */
	String context() default "";

	/**
	 * Specify the container name this attribute is subscribing from. It is
	 * required for <code>EPAttrType.Subscriber</code>.
	 * 
	 * @return String
	 */
	String container() default "";

	/**
	 * Specify the processor used to process these subscriptions. It is used to
	 * communicate with external world in the
	 * <code>EPAttrType.SubProcessor<code> 
	 * context and allows to override subscription in <code>EPAttrType.Subscriber<code>
	 * context.
	 * 
	 * @return String
	 */
	Class<? extends Object> processor() default Object.class;
}