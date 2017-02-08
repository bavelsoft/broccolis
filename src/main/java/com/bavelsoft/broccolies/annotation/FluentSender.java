package com.bavelsoft.broccolies.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(FluentSenders.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface FluentSender {
	Class value();
	String initializer() default "";
	Class reference() default Object.class;
	String sendMethod() default "";
}

