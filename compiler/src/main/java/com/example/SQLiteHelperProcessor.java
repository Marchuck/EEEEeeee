package com.example;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static com.example.Utils.getPackageName;
import static com.squareup.javapoet.JavaFile.builder;
import static java.util.Collections.singleton;
import static javax.lang.model.SourceVersion.latestSupported;
import static javax.tools.Diagnostic.Kind.ERROR;

@SupportedAnnotationTypes("*")
@AutoService(Processor.class)
public class SQLiteHelperProcessor extends AbstractProcessor {

    private static final String ANNOTATION = "@" + SQLiteTable.class.getSimpleName();

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return singleton(SQLiteTable.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<TypeElement> sqliteTables = new ArrayList<>();

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(SQLiteTable.class)) {
            if (annotatedElement instanceof TypeElement) {
                TypeElement annotatedClass = (TypeElement) annotatedElement;
                sqliteTables.add(annotatedClass);
            }
        }

        if (sqliteTables.size() > 0) {
            try {
                generate(sqliteTables);
            } catch (Exception e) {
                messager.printMessage(ERROR, "Couldn't generate class " + e.getMessage());
            }
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "no more tables to process");
        }

        return true;
    }

    private void generate(List<TypeElement> tables)
            throws NoPackageNameException, IOException {

        messager.printMessage(Diagnostic.Kind.NOTE, "processing " + tables.size() + " annotated classes");
        Element firstTable = tables.get(0);

        String packageName = getPackageName(processingEnv.getElementUtils(), firstTable);

        for (TypeElement nextAnnotatedTable : tables) {
            TypeSpec generatedClass = SQLiteOpenHelperGenerator.generateClass(messager, nextAnnotatedTable);
            JavaFile javaFile = builder(packageName, generatedClass).build();
            javaFile.writeTo(processingEnv.getFiler());
        }
    }

}

