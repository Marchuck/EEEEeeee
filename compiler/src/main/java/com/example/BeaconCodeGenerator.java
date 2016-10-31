package com.example;

import android.bluetooth.BluetoothAdapter;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

final class BeaconCodeGenerator {

    private static final String CLASS_NAME_SUFFIX = "BleScanner";

    public static TypeSpec generateClass(List<AnnotatedClass> classes) {
        TypeSpec.Builder builder = classBuilder(CLASS_NAME_SUFFIX)
                .addModifiers(PUBLIC, FINAL);
        for (AnnotatedClass anno : classes) {
            builder.addMethod(makeCreateStringMethod(anno));
        }
        return builder.build();
    }

    /**
     * @return a createString() method that takes annotatedClass's type as an input.
     */
    private static MethodSpec makeCreateStringMethod(AnnotatedClass annotatedClass) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("return \"%s{\" + ", annotatedClass.annotatedClassName));
        for (String variableName : annotatedClass.variableNames) {
            builder.append(String.format(" \"%s='\" + String.valueOf(instance.%s) + \"',\" + ",
                    variableName, variableName));
        }
        builder.append("\"}\"");
        return methodBuilder("createString")
                .addJavadoc("@return string suitable for {@param instance}'s toString()")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(TypeName.get(annotatedClass.getType()), "instance")
                .addStatement(builder.toString())
                .returns(String.class)
                .build();
    }

    public static TypeSpec generateClass(
            ProcessingEnvironment env,
            Element klazz, List<ExecutableElement> second, List<VariableElement> third) {

        // PackageElement elementqq = env.getElementUtils().getPackageOf(klazz);

        TypeName bleScanCallbackKlazz = ParameterizedTypeName.get(BluetoothAdapter.LeScanCallback.class);

        //    ClassName bleDevice = ClassName.get("android.bluetooth", "BluetoothDevice");
        //   TypeName bleDeviceKlazz = ParameterizedTypeName.get(Closeable.class);

        //  ClassName byteArrName = ClassName.get(Runnable.class);
        //   TypeName byteArrType = ParameterizedTypeName.get(byteArrName);

        TypeSpec.Builder builder = classBuilder(klazz.getSimpleName() + CLASS_NAME_SUFFIX)
                .addSuperinterface(bleScanCallbackKlazz)
                .addModifiers(Modifier.PUBLIC);

        StringBuilder code = new StringBuilder();
        for (ExecutableElement ex : second) {
            code.append("\n\tif (ref.get()!=null && device.getAddress().equals(\""
                    + ex.getAnnotation(WhenDetected.class).value() + "\")){\n");
            code.append("\t\t" +
                    "ref.get()." + ex.getSimpleName() + "();\n");
//                    "( (" + klazz.getSimpleName() + ") ref.get() )." + ex.getSimpleName() + "();\n");
            code.append("\t}");
        }

        ClassName weakReference = ClassName.get(WeakReference.class);
        TypeName ref = TypeName.get(klazz.asType());
        TypeName weakReferenceOfRef = ParameterizedTypeName.get(weakReference, ref);

        builder.addField(FieldSpec.builder(weakReferenceOfRef, "ref", Modifier.PRIVATE).build());

        builder.addMethod(MethodSpec.methodBuilder("init")
                .addModifiers(PUBLIC)
                .addParameter(TypeName.get(klazz.asType()), "scope")
                .addStatement("ref = new WeakReference<>(scope);\n")
//                .addStatement("// elementqq : " + elementqq.getQualifiedName().toString())
//                .addStatement("//enclosing elements: " + elementqq.getEnclosingElement().getSimpleName())
//                .addStatement("// enclosed elements: " + elementqq.getEnclosedElements().get(0).getSimpleName())
                .build());

//        builder.addMethod(MethodSpec.methodBuilder("onLeScan")
//                .addModifiers(Modifier.PUBLIC)
//                .addParameter(TypeName.OBJECT, "device", Modifier.FINAL)
//                .addParameter(TypeName.INT, "rssi", FINAL)
//                .addParameter(TypeName.BOOLEAN, "scanRecord", FINAL)
//                .addCode("if (ref != null) {\n")
//                .addCode(code.toString())
//                .addCode("\n}")
//                .build());

        builder.addMethod(MethodSpec.constructorBuilder()
                .addCode("\r  }\n")
                .addCode("\r  @Override" +
                        "\r  public void onLeScan(android.bluetooth.BluetoothDevice device, " +
                        "int rssi, byte[] scanRecord) {\n" + code +
                        "\n")
                .build());

//        builder.addMethod(MethodSpec.methodBuilder("get")
//                .addModifiers(PUBLIC)
//                .addStatement("return callback")
//                .returns(bleScanCallbackKlazz)
//                .build());

        return builder.build();
    }

}
