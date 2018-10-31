package com.AMS.jBEAM.javaParser;

class JavaParserSettings
{
    private final JavaInspectionDataProvider    inspectionDataProvider  = new JavaInspectionDataProvider();

    private final AbstractJavaEntityParser  javaExpressionParser;
    private final AbstractJavaEntityParser  fieldParser;
    private final AbstractJavaEntityParser  staticFieldParser;
	private final AbstractJavaEntityParser  methodParser;
	private final AbstractJavaEntityParser  staticMethodParser;
    private final AbstractJavaEntityParser  objectTailParser;

    JavaParserSettings(Class<?> thisContextClass) {
        javaExpressionParser = new JavaExpressionParser(this, thisContextClass);
        fieldParser = new JavaFieldParser(this, thisContextClass, false);
        staticFieldParser = new JavaFieldParser(this, thisContextClass, true);
		methodParser = new JavaMethodParser(this, thisContextClass, false);
		staticMethodParser = new JavaMethodParser(this, thisContextClass, true);
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

    AbstractJavaEntityParser getMethodParser(boolean staticOnly) {
    	return staticOnly ? staticMethodParser : methodParser;
	}

    AbstractJavaEntityParser getObjectTailParser() {
        return objectTailParser;
    }
}
