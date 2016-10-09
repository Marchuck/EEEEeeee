package com.example;

import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.PUBLIC;

final class RecursiveCodeGenerator {

    private static final String CLASS_NAME_SUFFIX = "22";

    public static TypeSpec generateClass(Element annotatedElement) {
        TypeSpec.Builder builder = classBuilder(annotatedElement.getSimpleName()+CLASS_NAME_SUFFIX)
                .addAnnotation(Recursive.class)
                .addModifiers(PUBLIC);
        return builder.build();
    }
}
