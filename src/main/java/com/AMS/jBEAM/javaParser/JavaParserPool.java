package com.AMS.jBEAM.javaParser;

class JavaParserPool
{
    private final JavaInspectionDataProvider    inspectionDataProvider  = new JavaInspectionDataProvider();

    private final EvaluationMode 				evaluationMode;

    private final AbstractJavaEntityParser  	javaExpressionParser;
    private final AbstractJavaEntityParser  	fieldParser;
    private final AbstractJavaEntityParser  	staticFieldParser;
	private final AbstractJavaEntityParser  	methodParser;
	private final AbstractJavaEntityParser  	staticMethodParser;
    private final AbstractJavaEntityParser  	objectTailParser;

    JavaParserPool(ObjectInfo thisInfo, EvaluationMode evaluationMode) {
    	this.evaluationMode = evaluationMode;

        javaExpressionParser = new JavaExpressionParser(this, thisInfo);
        fieldParser = new JavaFieldParser(this, thisInfo, false);
        staticFieldParser = new JavaFieldParser(this, thisInfo, true);
		methodParser = new JavaMethodParser(this, thisInfo, false);
		staticMethodParser = new JavaMethodParser(this, thisInfo, true);
        objectTailParser = new JavaObjectTailParser(this, thisInfo);
    }

    EvaluationMode getEvaluationMode() {
    	return evaluationMode;
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
