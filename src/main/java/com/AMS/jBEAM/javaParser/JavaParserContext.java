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

	private final AbstractEntityParser			javaExpressionParser;
	private final AbstractEntityParser			fieldParser;
	private final AbstractEntityParser			staticFieldParser;
	private final AbstractEntityParser			methodParser;
	private final AbstractEntityParser			staticMethodParser;
	private final AbstractEntityParser			tailParser;
	private final AbstractEntityParser			staticTailParser;
	private final AbstractEntityParser			literalParser;
	private final AbstractEntityParser			parenthesizedExpressionParser;
	private final AbstractEntityParser			castParser;
	private final AbstractEntityParser			classParser;
	private final AbstractEntityParser			constructorParser;

	JavaParserContext(ObjectInfo thisInfo, JavaParserSettings settings, EvaluationMode evaluationMode) {
		this.thisInfo = thisInfo;
		inspectionDataProvider 			= new InspectionDataProvider(settings);
		objectInfoProvider				= new ObjectInfoProvider(evaluationMode);
		fieldAndMethodDataProvider		= new FieldAndMethodDataProvider(this);
		classDataProvider				= new ClassDataProvider(this, settings.getImports());

		javaExpressionParser			= new ExpressionParser(this, thisInfo);
		fieldParser						= new FieldParser(this, thisInfo, false);
		staticFieldParser				= new FieldParser(this, thisInfo, true);
		methodParser					= new MethodParser(this, thisInfo, false);
		staticMethodParser				= new MethodParser(this, thisInfo, true);
		tailParser						= new TailParser(this, thisInfo, false);
		staticTailParser				= new TailParser(this, thisInfo, true);
		literalParser					= new LiteralParser(this, thisInfo);
		parenthesizedExpressionParser	= new ParenthesizedExpressionParser(this, thisInfo);
		castParser						= new CastParser(this, thisInfo);
		classParser						= new ClassParser(this, thisInfo);
		constructorParser				= new ConstructorParser(this, thisInfo);
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

	public AbstractEntityParser getExpressionParser() {
		return javaExpressionParser;
	}

	public AbstractEntityParser getFieldParser(boolean staticOnly) {
		return staticOnly ? staticFieldParser : fieldParser;
	}

	public AbstractEntityParser getMethodParser(boolean staticOnly) {
		return staticOnly ? staticMethodParser : methodParser;
	}

	public AbstractEntityParser getTailParser(boolean staticOnly) {
		return staticOnly ? staticTailParser : tailParser;
	}

	public AbstractEntityParser getLiteralParser() {
		return literalParser;
	}

	public AbstractEntityParser getParenthesizedExpressionParser() {
		return parenthesizedExpressionParser;
	}

	public AbstractEntityParser getCastParser() { return castParser; }

	public AbstractEntityParser getClassParser() { return classParser; }

	public AbstractEntityParser getConstructorParser() {
		return constructorParser;
	}
}
