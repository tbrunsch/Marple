package com.AMS.jBEAM.javaParser;

class JavaParserSettings
{
    private final JavaInspectionDataProvider    inspectionDataProvider  = new JavaInspectionDataProvider();

    private final JavaFieldParser               fieldParser;
    private final JavaFieldParser               staticFieldParser;
    private final JavaDotParser                 dotParser;
    private final JavaDotParser                 staticDotParser;

    JavaParserSettings(Class<?> thisContextClass) {
        dotParser = new JavaDotParser(this, thisContextClass, false);
        staticDotParser = new JavaDotParser(this, thisContextClass, true);
        fieldParser = new JavaFieldParser(this, thisContextClass, false);
        staticFieldParser = new JavaFieldParser(this, thisContextClass, true);
    }

    JavaInspectionDataProvider getInspectionDataProvider() {
        return inspectionDataProvider;
    }

    AbstractJavaEntityParser getFieldParser(boolean staticOnly) {
        return staticOnly ? staticFieldParser : fieldParser;
    }

    AbstractJavaEntityParser getDotParser(boolean staticOnly) {
        return staticOnly ? staticDotParser : dotParser;
    }
}
