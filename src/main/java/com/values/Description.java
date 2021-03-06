package com.values;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Manjunatha P on 18/12/2016.
 */

@Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) public @interface Description {

    String value() default "";
}
