package com.bavelsoft.broccolies.annotation;

import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;

@Target(TYPE)
public @interface FluentActor {
	String value();
	String initializer() default "";
	Class reference() default Object.class;
	boolean legacyCompatible() default false;
}

