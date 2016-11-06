package com.example;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;
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
 * <p>
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


        addConstructor(builder);

        addOverrideMethods(builder, table);

        addStaticFields(table, messager, builder);

        addADDMethod(builder, table, name);
        addREADMethod(builder, table, name);
        addUPDATEMethod(builder, table, name);
        addDELETEMethod(builder, table, name);

        return builder
                .superclass(TypeName.get(SQLiteOpenHelper.class))
                .build();
    }

    private static void addConstructor(TypeSpec.Builder builder) {
        TypeName contextTypeName = TypeName.get(Context.class);

        builder.addMethod(
                MethodSpec.constructorBuilder()
                        .addParameter(
                                contextTypeName, "context"
                        )
                        .addCode("super(context, DATABASE_NAME, null, DATABASE_VERSION);")
                        .addModifiers(PUBLIC)
                        .build()
        );
    }

    private static void addOverrideMethods(TypeSpec.Builder builder, TypeElement table) {
        TypeName sqliteDatabaseTypeName = TypeName.get(SQLiteDatabase.class);

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
                        .addCode("database.execSQL(\"DROP TABLE IF EXISTS \" + " + getTableFieldName(table) + ");\n")
                        .addCode("onCreate(database);")
                        .build()
        );
    }

    private static void addADDMethod(TypeSpec.Builder builder, TypeElement table, String name) {
        String camelCasedName = camelCased(table.getSimpleName().toString());
        builder.addMethod(
                MethodSpec.methodBuilder("add" + camelCasedName)
                        .addModifiers(PUBLIC)
                        .addJavadoc("Adds " + camelCasedName + " to table\n")
                        .addParameter(
                                ParameterSpec.builder(
                                        TypeName.get(Object.class), "new" + camelCased(name)
                                ).build()
                        )
                        .addParameter(
                                ParameterSpec.builder(
                                        TypeName.BOOLEAN, "shouldCloseDatabase"
                                ).build()
                        )
                        .addCode(name + " " + name.toLowerCase() + " = (" + name + ") " + "new" + camelCased(name) + ";\n")
                        .addCode("SQLiteDatabase database = this.getWritableDatabase();\n")
                        .addCode("android.content.ContentValues values = new android.content.ContentValues();\n")
                        .addCode(contentValuesFill("values", name.toLowerCase(), table))
                        .addCode("database.insert(" + getTableFieldName(table) + ", null, values);")
                        .addCode("\nif(shouldCloseDatabase) database.close();")
                        .addCode("//todo: TEST THIS!!!\n")
                        .returns(TypeName.VOID)
                        .build()
        );

    }


    private static void addDELETEMethod(TypeSpec.Builder builder, TypeElement table, String name) {

        Element primaryKey = getPrimaryKey(table);
        String keyName = "KEY_" + primaryKey.getAnnotation(SQLiteField.class).value().toUpperCase();


//        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;

        builder.addMethod(
                MethodSpec.methodBuilder("delete" + camelCased(name))
                        .addJavadoc("Deletes object with given primary key\n" +
                                "if object is succesfully deleted\n" +
                                "result is true, false otherwise")
                        .addModifiers(PUBLIC)
                        .addParameter(ParameterSpec.builder(TypeName.get(Object.class), "_"
                                + name.toLowerCase()).build())
                        .addCode(name + " " + name.toLowerCase() + " = (" + name + ") " + "_"
                                + name.toLowerCase() + ";\n")

                        .addCode("\nSQLiteDatabase database = this.getWritableDatabase();\n")
                        .addCode("boolean result =  database.delete("
                                + getTableFieldName(table) + ", "
                                + keyName + "+\"=\"+"
                                + name.toLowerCase() + ",null) > 0;\n")
                        .addCode("return result;\n")
                        .returns(TypeName.BOOLEAN)
                        .build()
        );
    }


    private static void addUPDATEMethod(TypeSpec.Builder builder, TypeElement table, String name) {

        Element primaryKey = getPrimaryKey(table);
        String keyName = "KEY_" + primaryKey.getAnnotation(SQLiteField.class).value().toUpperCase();


        builder.addMethod(
                MethodSpec.methodBuilder("update" + camelCased(name))
                        .addModifiers(PUBLIC)
                        .addParameter(ParameterSpec.builder(TypeName.get(Object.class), "_" + name.toLowerCase()).build())
                        .addCode(name + " " + name.toLowerCase() + " = (" + name + ") " + "_" + name.toLowerCase() + ";\n")

                        .addCode("\nSQLiteDatabase database = this.getWritableDatabase();\n")
                        .addCode("android.content.ContentValues values = new android.content.ContentValues();\n")
                        .addCode(contentValuesFill("values", name.toLowerCase(), table))
                        .addCode("int result =  database.update(" + getTableFieldName(table) + ", values, "
                                + keyName + "+\" = ?\", \nnew String[] { String.valueOf("
                                + name.toLowerCase() + ".get" + camelCased(primaryKey.getSimpleName().toString())
                                + "()) });")
                        .returns(TypeName.VOID)
                        .build()
        );
    }


    private static void addREADMethod(TypeSpec.Builder builder, TypeElement table, String name) {


        builder.addMethod(
                MethodSpec.methodBuilder("get" + camelCased(table.getSimpleName().toString()))
                        .addModifiers(PUBLIC)
                        .addParameter(
                                ParameterSpec.builder(
                                        TypeName.get(String.class), "_key"
                                ).build()
                        )
                        .addParameter(
                                ParameterSpec.builder(
                                        TypeName.get(String.class), "value"
                                ).build()
                        )
                        //  .addCode(name + " " + name.toLowerCase() + " = (" + name + ") " + "new " + camelCased(name) + "();\n")
                        .addCode("\nSQLiteDatabase database = this.getReadableDatabase();\n")
                        .addCode("android.database.Cursor cursor = database.query(" + getTableFieldName(table)
                                + ", new String[] {\n")
                        .addCode(columnsOfThisTable(table))
                        .addCode("}\n, _key + \"=?\",\n")
                        .addCode("new String[] { String.valueOf(value) }, null, null, null, null);\n")
                        .addCode("if(cursor !=null) cursor.moveToFirst();\n")
                        .addCode("else return null;\n")
                        .addCode(name + " " + name.toLowerCase() + " = new " + name + "();\n")
                        .addCode(fillFiledsInThisAnnotatedKlazz(name.toLowerCase(), "cursor", table))
                        .addCode("\nreturn " + name.toLowerCase() + ";\n")
                        .returns(TypeName.get(Object.class))
                        .build()
        );
    }

    private static String fillFiledsInThisAnnotatedKlazz(String variableName, String cursorName, TypeElement table) {
        StringBuilder sb = new StringBuilder();
        List<Element> fields = getAnnotatedFieldsOf(SQLiteField.class, table.getEnclosedElements());
        int index = 0;
        for (Element element : fields) {
            String fieldName = camelCased(element.getSimpleName().toString());
            SQLiteField sqLiteField = element.getAnnotation(SQLiteField.class);
            String type = sqLiteField.type();
            String forSqLiteReadableType;
            if (type.equals("INT")) {
                forSqLiteReadableType = "Int";
            } else if (type.equals("LONG")) {
                forSqLiteReadableType = "Long";
            } else if (type.equals("SHORT")) {
                forSqLiteReadableType = "Short";
            } else if (type.equals("DOUBLE")) {
                forSqLiteReadableType = "Double";
            } else {
                forSqLiteReadableType = "String";

            }
            /* for instance: pojo    .setUuid(   cursor   .getString   (0)); */
            sb.append(variableName)
                    .append(".set").append(fieldName).append("(")

                    .append(cursorName)

                    .append(".get").append(forSqLiteReadableType)

                    .append("(").append(index).append(") );\n");
            index++;
        }
        return sb.toString();
    }

    private static String columnsOfThisTable(TypeElement table) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Element> fields = getAnnotatedFieldsOf(SQLiteField.class, table.getEnclosedElements());
        for (int j = 0; j < fields.size(); j++) {
            Element e = fields.get(j);
            SQLiteField sqLiteField = e.getAnnotation(SQLiteField.class);
            String fieldName = sqLiteField.value();
            String keyName = "KEY_" + fieldName.toUpperCase();

            stringBuilder.append(keyName).append(",\n");
        }

        return stringBuilder.toString();
    }

    private static String contentValuesFill(String valuesVariableName, String newObjectName, TypeElement table) {
        StringBuilder sb = new StringBuilder();

        for (Element e : table.getEnclosedElements()) {
            if (e.getKind() == ElementKind.FIELD) {

                SQLiteField annotatedField = e.getAnnotation(SQLiteField.class);

                if (annotatedField != null && annotatedField.value() != null) {
                    String fieldNameCamelCased = camelCased(e.getSimpleName().toString());
                    String keyName = "KEY_" + annotatedField.value().toUpperCase();

                    sb.append(valuesVariableName)
                            .append(".put(").append(keyName)
                            .append(",")
                            .append(newObjectName).append(".get").append(fieldNameCamelCased).append("() );\n");
                } else {
                    //got other field that wasn't annotated with SQLiteField
                }
            }
        }
        return sb.toString();
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

        builder.addField(FieldSpec.builder(String.class, getTableFieldName(table), PUBLIC, STATIC, FINAL)
                .initializer(asLiteralString(getTableFieldValue(table)))
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
        schema.append(getTableFieldValue(table));
        schema.append("( ");

        List<? extends Element> enclosedElements = table.getEnclosedElements();

        List<Element> annotatedElements = getAnnotatedFieldsOf(SQLiteField.class, enclosedElements);

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

    private static <T extends Annotation>
    List<Element> getAnnotatedFieldsOf(Class<T> klazz, List<? extends Element> enclosedElements) {
        List<Element> annotatedElements = new ArrayList<>();
        for (Element e : enclosedElements) {
            if (e.getKind() == ElementKind.FIELD) {
                T annotatedField = e.getAnnotation(klazz);
                if (annotatedField != null) {
                    annotatedElements.add(e);
                }
            }
        }
        return annotatedElements;
    }

    private static void appendNextField(Element firstElement, StringBuilder schema) {
        SQLiteField annotatedField = firstElement.getAnnotation(SQLiteField.class);

        String fieldName = annotatedField.value();
        String fieldType = annotatedField.type();

        schema.append(fieldName);
        schema.append(' ');
        schema.append(fieldType);

        if (isPrimaryKey(firstElement)) {
            schema.append(" primary key");
        }
    }

    private static boolean isPrimaryKey(Element e) {
        return elementHasAnnotation(e, SQLitePrimaryKey.class);
    }

    private static boolean elementHasAnnotation(Element e, Class<? extends Annotation> annotation) {
        return e.getAnnotation(annotation) != null;
    }


    private static Element getPrimaryKey(TypeElement table) {
        for (Element e : table.getEnclosedElements()) {
            if (isPrimaryKey(e)) return e;
        }
        throw new NoPrimaryKeyException();
    }


    private static String getTableFieldValue(TypeElement table) {
        SQLiteTable name = table.getAnnotation(SQLiteTable.class);
        return name == null ? table.getSimpleName().toString().toLowerCase() : name.value();
    }

    private static String getTableFieldName(TypeElement table) {
        return "TABLE_" + table.getSimpleName().toString().toUpperCase();
    }
}
