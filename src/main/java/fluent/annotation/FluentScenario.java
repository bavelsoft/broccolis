package broccolies.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(FluentScenarios.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface FluentScenario {
	Class value();
	String sendMethod() default "";
}

