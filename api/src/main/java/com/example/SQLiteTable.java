package com.example;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Project "Annotations101"
 * <p>
 * Created by Lukasz Marczak
 * on 09.10.16.
 */

@Target(value = {TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SQLiteTable {
    String value();
}
