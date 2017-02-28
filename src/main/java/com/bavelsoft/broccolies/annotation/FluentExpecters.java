package com.bavelsoft.broccolies.annotation;

import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.METHOD;

@Target(METHOD)
public @interface FluentExpecters {
	FluentExpecter[] value() default {};
}

