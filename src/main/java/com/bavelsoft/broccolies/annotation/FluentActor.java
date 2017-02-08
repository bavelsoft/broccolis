package com.bavelsoft.broccolies.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FluentActor {
	String value();
	String initializer() default "";
	Class reference() default Object.class;
	boolean legacyCompatible() default false;
}

