package com.bavelsoft.broccolies.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(FluentExpecters.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface FluentExpecter {
	Class value();
	Class reference() default Object.class;
	String onlyLastOf() default "";
	String expectMethod() default "";
}

