package org.fastcatsearch.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target ({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)

public @interface ActionMapping {
	String value();
	ActionMethod[] method() default {ActionMethod.GET, ActionMethod.POST};
}
