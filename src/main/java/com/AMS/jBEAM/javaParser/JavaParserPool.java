package com.AMS.jBEAM.javaParser;

class JavaParserPool
{
	// TODO: Make configurable
	private final Imports						imports					= new Imports();

	private final JavaInspectionDataProvider	inspectionDataProvider  = new JavaInspectionDataProvider();

	private final EvaluationMode				evaluationMode;

	private final AbstractJavaEntityParser		javaExpressionParser;
	private final AbstractJavaEntityParser		fieldParser;
	private final AbstractJavaEntityParser		staticFieldParser;
	private final AbstractJavaEntityParser		methodParser;
	private final AbstractJavaEntityParser		staticMethodParser;
	private final AbstractJavaEntityParser		literalParser;
	private final AbstractJavaEntityParser		objectTailParser;
	private final AbstractJavaEntityParser		parenthesizedExpressionParser;
	private final AbstractJavaEntityParser 		castParser;

	JavaParserPool(ObjectInfo thisInfo, EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;

		javaExpressionParser = new JavaExpressionParser(this, thisInfo);
		fieldParser = new JavaFieldParser(this, thisInfo, false);
		staticFieldParser = new JavaFieldParser(this, thisInfo, true);
		methodParser = new JavaMethodParser(this, thisInfo, false);
		staticMethodParser = new JavaMethodParser(this, thisInfo, true);
		literalParser = new JavaLiteralParser(this, thisInfo);
		objectTailParser = new JavaObjectTailParser(this, thisInfo);
		parenthesizedExpressionParser = new JavaParenthesizedExpressionParser(this, thisInfo);
		castParser = new JavaCastParser(this, thisInfo);
	}

	EvaluationMode getEvaluationMode() {
		return evaluationMode;
	}

	JavaInspectionDataProvider getInspectionDataProvider() {
		return inspectionDataProvider;
	}

	Imports getImports() {
		return imports;
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

	AbstractJavaEntityParser getLiteralParser() {
		return literalParser;
	}

	AbstractJavaEntityParser getObjectTailParser() {
		return objectTailParser;
	}

	AbstractJavaEntityParser getParenthesizedExpressionParser() {
		return parenthesizedExpressionParser;
	}

	AbstractJavaEntityParser getCastParser() { return castParser; }
}
