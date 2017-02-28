package com.bavelsoft.broccolies.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;

@Target(METHOD)
@Repeatable(FluentNestedSenders.class)
public @interface FluentNestedSender {
	Class value();
	Class[] containers();
	String initializer() default "";
}

