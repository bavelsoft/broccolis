package broccolies.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(FluentNestedSenders.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface FluentNestedSender {
	Class value();
	Class[] containers();
	String initializer() default "";
}

