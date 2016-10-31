package com.example;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

import static com.example.Utils.getPackageName;
import static com.squareup.javapoet.JavaFile.builder;
import static javax.lang.model.SourceVersion.latestSupported;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public class BluetoothCallbacksProcessor extends AbstractProcessor {

    private static final String ANNOTATION = "@" + WhenDetected.class.getSimpleName();

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {

        return new HashSet<>(Arrays.asList(WhenDetected.class.getCanonicalName(),
                BluetoothScope.class.getCanonicalName()));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        List<Three<Element, List<ExecutableElement>, List<VariableElement>>> all = new ArrayList<>();
        for (Element root : roundEnv.getElementsAnnotatedWith(BluetoothScope.class)) {

            List<ExecutableElement> executableElements = new ArrayList<>();

            for (Element element : root.getEnclosedElements()) {

                if (element.getKind() == ElementKind.METHOD) {

                    if (element.getAnnotation(WhenDetected.class) != null) {

                    }
                }
            }

            for (Element nestedElement : roundEnv.getElementsAnnotatedWith(WhenDetected.class)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "klazz " + nestedElement.getSimpleName().toString());

                List<ExecutableElement> annotatedMethods = new ArrayList<>();
                List<VariableElement> annotatedFields = new ArrayList<>();

                if (nestedElement.getKind() == ElementKind.FIELD) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "field " + nestedElement.getSimpleName().toString());

                    annotatedFields.add((VariableElement) nestedElement);

                } else if (nestedElement.getKind() == ElementKind.METHOD) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "method " + nestedElement.getSimpleName().toString());

                    annotatedMethods.add((ExecutableElement) nestedElement);
                }

                all.add(new Three<>(root, annotatedMethods, annotatedFields));

            }
        }

        generate(all);

        return true;
    }

    private boolean isValidClass(TypeElement annotatedClass) {

        if (!ClassValidator.isPublic(annotatedClass)) {
            String message = String.format("Classes annotated with %s must be public.",
                    ANNOTATION);
            messager.printMessage(ERROR, message, annotatedClass);
            return false;
        }

        if (ClassValidator.isAbstract(annotatedClass)) {
            String message = String.format("Classes annotated with %s must not be abstract.",
                    ANNOTATION);
            messager.printMessage(ERROR, message, annotatedClass);
            return false;
        }

        return true;
    }

    private AnnotatedClass buildAnnotatedClass(TypeElement annotatedClass)
            throws NoPackageNameException, IOException {
        ArrayList<String> variableNames = new ArrayList<>();
        for (Element element : annotatedClass.getEnclosedElements()) {
            if (!(element instanceof VariableElement)) {
                continue;
            }
            VariableElement variableElement = (VariableElement) element;
            variableNames.add(variableElement.getSimpleName().toString());
        }
        return new AnnotatedClass(annotatedClass, variableNames);
    }

    private void generate(List<Three<Element, List<ExecutableElement>, List<VariableElement>>> all) {
        // String packageName = getPackageName(processingEnv.getElementUtils(), three.first);
        messager.printMessage(Diagnostic.Kind.NOTE, "generating now in ");
        try {
            for (Three<Element, List<ExecutableElement>, List<VariableElement>> three : all) {

                String packageName = getPackageName(processingEnv.getElementUtils(), three.first);

                TypeSpec generatedClass = BeaconCodeGenerator.generateClass(processingEnv, three.first, three.second, three.third);

                JavaFile javaFile = builder(packageName, generatedClass).build();
                javaFile.writeTo(processingEnv.getFiler());

            }
        } catch (Exception v) {
            messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Sth went wrong " + v.getMessage());
        }

    }

    static int x;
}

