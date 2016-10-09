package com.example;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;

final class Utils {

    private Utils() {
        // no instances
    }

    static String getPackageName(Elements elementUtils, Element type)
            throws NoPackageNameException {
        PackageElement pkg = elementUtils.getPackageOf(type);
        if (pkg.isUnnamed()) {
            throw new RuntimeException(type.getSimpleName().toString());
        }
        return pkg.getQualifiedName().toString();
    }
}
