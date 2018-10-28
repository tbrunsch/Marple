package com.AMS.jBEAM.javaParser;

class JavaParserSettings
{
    private final JavaInspectionDataProvider    inspectionDataProvider  = new JavaInspectionDataProvider();

    private final AbstractJavaEntityParser  javaExpressionParser;
    private final AbstractJavaEntityParser  fieldParser;
    private final AbstractJavaEntityParser  staticFieldParser;
    private final AbstractJavaEntityParser  dotParser;
    private final AbstractJavaEntityParser  staticDotParser;
    private final AbstractJavaEntityParser  arrayAccessParser;
    private final AbstractJavaEntityParser  objectTailParser;

    JavaParserSettings(Class<?> thisContextClass) {
        javaExpressionParser = new JavaExpressionParser(this, thisContextClass);
        fieldParser = new JavaFieldParser(this, thisContextClass, false);
        staticFieldParser = new JavaFieldParser(this, thisContextClass, true);
        dotParser = new JavaDotParser(this, thisContextClass, false);
        staticDotParser = new JavaDotParser(this, thisContextClass, true);
        arrayAccessParser = new JavaArrayAccessParser(this, thisContextClass);
        objectTailParser = new JavaObjectTailParser(this, thisContextClass);
    }

    JavaInspectionDataProvider getInspectionDataProvider() {
        return inspectionDataProvider;
    }

    AbstractJavaEntityParser getExpressionParser() {
        return javaExpressionParser;
    }

    AbstractJavaEntityParser getFieldParser(boolean staticOnly) {
        return staticOnly ? staticFieldParser : fieldParser;
    }

    AbstractJavaEntityParser getDotParser(boolean staticOnly) {
        return staticOnly ? staticDotParser : dotParser;
    }

    AbstractJavaEntityParser getArrayAccessParser() {
        return arrayAccessParser;
    }

    AbstractJavaEntityParser getObjectTailParser() {
        return objectTailParser;
    }
}
