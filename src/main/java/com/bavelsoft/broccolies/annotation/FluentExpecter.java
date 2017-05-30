package com.bavelsoft.broccolies.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;

@Target(METHOD)
@Repeatable(FluentExpecters.class)
public @interface FluentExpecter {
	Class value();
	Class reference() default Object.class;
	String onlyLastOf() default "";
	String expectMethod() default "";
	String[] excludes() default {};
}

