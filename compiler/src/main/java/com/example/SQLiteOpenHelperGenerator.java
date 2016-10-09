package com.example;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Project "Annotations101"
 * <p/>
 * Created by Lukasz Marczak
 * on 09.10.16.
 */
public class SQLiteOpenHelperGenerator {

    public static final String helperSuffix = "OpenHelper";

    public static TypeSpec generateClass(Messager messager, TypeElement table) {


        String name = table.getSimpleName().toString();
        String className = name + helperSuffix;
        messager.printMessage(Diagnostic.Kind.NOTE, "creating class " + className);

        TypeSpec.Builder builder = classBuilder(className)
                .addJavadoc("code is generated, edit at your own risk\n\n")
                .addJavadoc("created with SQLight")
                .addModifiers(PUBLIC);

        TypeName contextTypeName = TypeName.get(Context.class);
        TypeName sqliteDatabaseTypeName = TypeName.get(SQLiteDatabase.class);

        builder.addMethod(
                MethodSpec.constructorBuilder()
                        .addParameter(
                                contextTypeName, "context"
                        )
                        .addCode("super(context, DATABASE_NAME, null, DATABASE_VERSION);")
                        .addModifiers(PUBLIC)
                        .build()
        );

        builder.addMethod(
                MethodSpec.methodBuilder("onCreate")
                        .addParameter(sqliteDatabaseTypeName, "database")
                        .addModifiers(PUBLIC)
                        .addAnnotation(Override.class)
                        .addCode(
                                " database.execSQL(DATABASE_CREATE_SCHEMA);"
                        )
                        .build()
        );

        builder.addMethod(
                MethodSpec.methodBuilder("onUpgrade")
                        .addParameter(sqliteDatabaseTypeName, "database")
                        .addParameter(TypeName.INT, "oldVersion")
                        .addParameter(TypeName.INT, "newVersion")
                        .addModifiers(PUBLIC)
                        .addAnnotation(Override.class)
                        .addCode("database.execSQL(\"DROP TABLE IF EXISTS \" + TABLE_NAME);\n")
                        .addCode("onCreate(database);")
                        .build()
        );

        addStaticFields(table, messager, builder);

        builder.addMethod(
                MethodSpec.methodBuilder("add" + camelCased(table.getSimpleName().toString()))
                        .addModifiers(PUBLIC)
                        .addParameter(
                                ParameterSpec.builder(
                                        TypeName.get(Object.class), "new" + camelCased(name)
                                ).build()
                        )
                        .addCode(name + " " + name.toLowerCase() + " = (" + name + ") " + "new" + camelCased(name)+";\n")
                        .addCode("//todo: dokończyć\n")
                        .returns(TypeName.VOID)
                        .build()
        );

        return builder
                .superclass(TypeName.get(SQLiteOpenHelper.class))
                .build();
    }

    private static String camelCased(String simpleName) {
        StringBuilder stringBuilder = new StringBuilder(simpleName.toLowerCase());
        stringBuilder.replace(0, 1, String.valueOf(simpleName.charAt(0)).toUpperCase());
        return stringBuilder.toString();

    }

    private static void addStaticFields(TypeElement table, Messager messager, TypeSpec.Builder builder) {


        for (Element field : table.getEnclosedElements()) {

            if (field.getKind() == ElementKind.FIELD) {

                messager.printMessage(Diagnostic.Kind.NOTE, "new field " + field.getSimpleName());
                SQLiteField sqLiteField = field.getAnnotation(SQLiteField.class);

                if (sqLiteField != null && sqLiteField.value() != null) {

                    String fieldName = sqLiteField.value();
                    messager.printMessage(Diagnostic.Kind.NOTE, "adding final field " + fieldName);
                    String keyName = "KEY_" + fieldName.toUpperCase();

                    builder.addField(FieldSpec.builder(String.class, keyName, PUBLIC, STATIC, FINAL)
                            .initializer("\"" + fieldName + "\"")
                            .build());
                }
            }
        }

        builder.addField(FieldSpec.builder(String.class, "DATABASE_NAME", PUBLIC, STATIC, FINAL)
                .initializer(asLiteralString("ANDROID_GENERATED_DATABASE"))
                .build());

        builder.addField(FieldSpec.builder(TypeName.INT, "DATABASE_VERSION", PUBLIC, STATIC, FINAL)
                .initializer((String.valueOf(1)))
                .build());

        builder.addField(FieldSpec.builder(String.class, "TABLE_NAME", PUBLIC, STATIC, FINAL)
                .initializer(asLiteralString(getTableName(table)))
                .build());

        builder.addField(FieldSpec.builder(String.class, "DATABASE_CREATE_SCHEMA", PUBLIC, STATIC, FINAL)
                .initializer(asLiteralString(generateTableSchema(table)))
                .build());
    }

    private static String asLiteralString(String s) {
        return "\"" + s + "\"";
    }

    private static String generateTableSchema(TypeElement table) {

        StringBuilder schema = new StringBuilder();
        schema.append("CREATE TABLE");
        schema.append(' ');
        schema.append(getTableName(table));
        schema.append("( ");


        List<? extends Element> enclosedElements = table.getEnclosedElements();
        List<Element> annotatedElements = new ArrayList<>();
        for (Element e : enclosedElements) {
            if (e.getKind() == ElementKind.FIELD) {
                SQLiteField annotatedField = e.getAnnotation(SQLiteField.class);
                if (annotatedField != null) {
                    annotatedElements.add(e);
                }
            }
        }
        if (annotatedElements.size() == 0)
            throw new IllegalStateException("At least one field should be in  table " + table.getSimpleName());

        Element firstElement = annotatedElements.get(0);

        appendNextField(firstElement, schema);

        for (int i = 1; i < annotatedElements.size(); i++) {
            Element element = annotatedElements.get(i);

            schema.append(", ");

            appendNextField(element, schema);
        }

        return schema.append(");").toString();
    }

    private static void appendNextField(Element firstElement, StringBuilder schema) {
        SQLiteField annotatedField = firstElement.getAnnotation(SQLiteField.class);

        String fieldName = annotatedField.value();
        String fieldType = annotatedField.type();

        schema.append(fieldName);
        schema.append(' ');
        schema.append(fieldType);

        if (firstElement.getAnnotation(SQLitePrimaryKey.class) != null) {
            schema.append(" primary key");
        }
    }


    private static String getTableName(TypeElement table) {
        SQLiteTable name = table.getAnnotation(SQLiteTable.class);
        return name == null ? table.getSimpleName().toString().toLowerCase() : name.value();
    }
}
