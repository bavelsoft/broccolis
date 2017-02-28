package com.bavelsoft.broccolies.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;

@Target(METHOD)
@Repeatable(FluentScenarios.class)
public @interface FluentScenario {
	Class value();
	String sendMethod() default "";
}

