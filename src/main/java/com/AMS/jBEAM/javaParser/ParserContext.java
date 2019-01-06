package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.parsers.*;
import com.AMS.jBEAM.javaParser.utils.*;

public class ParserContext
{
	private final ObjectInfo					thisInfo;
	private final ParserSettings 				settings;

	private final InspectionDataProvider 		inspectionDataProvider;
	private final ObjectInfoProvider			objectInfoProvider;
	private final FieldDataProvider				fieldDataProvider;
	private final MethodDataProvider			methodDataProvider;
	private final ClassDataProvider				classDataProvider;
	private final VariableDataProvider			variableDataProvider;
	private final OperatorResultProvider 		operatorResultProvider;

	private final AbstractEntityParser 			expressionParser;
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
	private final AbstractEntityParser			variableParser;
	private final AbstractEntityParser			constructorParser;
	private final AbstractEntityParser			unaryPrefixOperatorParser;
	private final AbstractEntityParser			compoundExpressionParser;

	public ParserContext(ObjectInfo thisInfo, ParserSettings settings, EvaluationMode evaluationMode) {
		this.thisInfo = thisInfo;
		this.settings = settings;

		inspectionDataProvider 			= new InspectionDataProvider(settings);
		objectInfoProvider				= new ObjectInfoProvider(evaluationMode);
		fieldDataProvider				= new FieldDataProvider(this);
		methodDataProvider				= new MethodDataProvider(this);
		classDataProvider				= new ClassDataProvider(this, settings.getImports());
		variableDataProvider			= new VariableDataProvider(settings.getVariablePool());
		operatorResultProvider 			= new OperatorResultProvider(evaluationMode);

		expressionParser 				= new ExpressionParser(this, thisInfo);
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
		variableParser					= new VariableParser(this, thisInfo);
		constructorParser				= new ConstructorParser(this, thisInfo);
		unaryPrefixOperatorParser		= new UnaryPrefixOperatorParser(this, thisInfo);
		compoundExpressionParser		= createCompoundExpressionParser(Integer.MAX_VALUE);
	}

	public ObjectInfo getThisInfo() {
		return thisInfo;
	}

	public ParserSettings getSettings() {
		return settings;
	}

	public InspectionDataProvider getInspectionDataProvider() {
		return inspectionDataProvider;
	}

	public ObjectInfoProvider getObjectInfoProvider() {
		return objectInfoProvider;
	}

	public FieldDataProvider getFieldDataProvider() {
		return fieldDataProvider;
	}

	public MethodDataProvider getMethodDataProvider() {
		return methodDataProvider;
	}

	public ClassDataProvider getClassDataProvider() {
		return classDataProvider;
	}

	public VariableDataProvider getVariableDataProvider() {
		return variableDataProvider;
	}

	public OperatorResultProvider getOperatorResultProvider() {
		return operatorResultProvider;
	}

	public AbstractEntityParser getExpressionParser() {
		return expressionParser;
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

	public AbstractEntityParser getVariableParser() { return variableParser; }

	public AbstractEntityParser getConstructorParser() {
		return constructorParser;
	}

	public AbstractEntityParser getCompoundExpressionParser() {
		return compoundExpressionParser;
	}

	public AbstractEntityParser getUnaryPrefixOperatorParser() {
		return unaryPrefixOperatorParser;
	}

	public AbstractEntityParser createCompoundExpressionParser(int maxOperatorPrecedenceLevelToConsider) {
		return new CompoundExpressionParser(this, thisInfo, maxOperatorPrecedenceLevelToConsider);
	}
}
