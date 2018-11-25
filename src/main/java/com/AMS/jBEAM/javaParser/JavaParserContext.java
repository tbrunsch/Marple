package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.parsers.*;
import com.AMS.jBEAM.javaParser.utils.*;

public class JavaParserContext
{
	private final ObjectInfo					thisInfo;
	private final InspectionDataProvider 		inspectionDataProvider;
	private final ObjectInfoProvider			objectInfoProvider;
	private final FieldAndMethodDataProvider	fieldAndMethodDataProvider;
	private final ClassDataProvider				classDataProvider;

	private final AbstractJavaEntityParser javaExpressionParser;
	private final AbstractJavaEntityParser		fieldParser;
	private final AbstractJavaEntityParser		staticFieldParser;
	private final AbstractJavaEntityParser		methodParser;
	private final AbstractJavaEntityParser		staticMethodParser;
	private final AbstractJavaEntityParser		literalParser;
	private final AbstractJavaEntityParser		objectTailParser;
	private final AbstractJavaEntityParser		parenthesizedExpressionParser;
	private final AbstractJavaEntityParser 		castParser;

	JavaParserContext(ObjectInfo thisInfo, JavaParserSettings settings, EvaluationMode evaluationMode) {
		this.thisInfo = thisInfo;
		inspectionDataProvider  = new InspectionDataProvider(settings);
		objectInfoProvider = new ObjectInfoProvider(evaluationMode);
		fieldAndMethodDataProvider = new FieldAndMethodDataProvider(this);
		classDataProvider = new ClassDataProvider(this, settings.getImports());

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

	public ObjectInfo getThisInfo() {
		return thisInfo;
	}

	public InspectionDataProvider getInspectionDataProvider() {
		return inspectionDataProvider;
	}

	public ObjectInfoProvider getObjectInfoProvider() {
		return objectInfoProvider;
	}

	public FieldAndMethodDataProvider getFieldAndMethodDataProvider() {
		return fieldAndMethodDataProvider;
	}

	public ClassDataProvider getClassDataProvider() {
		return classDataProvider;
	}

	public AbstractJavaEntityParser getExpressionParser() {
		return javaExpressionParser;
	}

	public AbstractJavaEntityParser getFieldParser(boolean staticOnly) {
		return staticOnly ? staticFieldParser : fieldParser;
	}

	public AbstractJavaEntityParser getMethodParser(boolean staticOnly) {
		return staticOnly ? staticMethodParser : methodParser;
	}

	public AbstractJavaEntityParser getLiteralParser() {
		return literalParser;
	}

	public AbstractJavaEntityParser getObjectTailParser() {
		return objectTailParser;
	}

	public AbstractJavaEntityParser getParenthesizedExpressionParser() {
		return parenthesizedExpressionParser;
	}

	public AbstractJavaEntityParser getCastParser() { return castParser; }
}
