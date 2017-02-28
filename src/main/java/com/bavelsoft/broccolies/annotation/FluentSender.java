package com.bavelsoft.broccolies.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;

@Target(METHOD)
@Repeatable(FluentSenders.class)
public @interface FluentSender {
	Class value();
	String initializer() default "";
	Class reference() default Object.class;
	String sendMethod() default "";
}

