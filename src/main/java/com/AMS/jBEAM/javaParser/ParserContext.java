package com.AMS.jBEAM.javaParser;

import com.AMS.jBEAM.javaParser.parsers.*;
import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import com.AMS.jBEAM.javaParser.settings.ParserSettings;
import com.AMS.jBEAM.javaParser.utils.dataProviders.*;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.google.common.reflect.TypeToken;

public class ParserContext
{
	private final ObjectInfo							thisInfo;
	private final ParserSettings						settings;

	private final ClassDataProvider						classDataProvider;
	private final ExecutableDataProvider				executableDataProvider;
	private final FieldDataProvider						fieldDataProvider;
	private final InspectionDataProvider				inspectionDataProvider;
	private final ObjectInfoProvider					objectInfoProvider;
	private final ObjectTreeNodeDataProvider			objectTreeNodeDataProvider;
	private final OperatorResultProvider 				operatorResultProvider;
	private final VariableDataProvider					variableDataProvider;

	private final AbstractEntityParser<ObjectInfo>		castParser;
	private final AbstractEntityParser<TypeToken<?>>	classFieldParser;
	private final AbstractEntityParser<TypeToken<?>>	classMethodParser;
	private final AbstractEntityParser<TypeToken<?>>	classTailParser;
	private final AbstractEntityParser<ObjectInfo>		compoundExpressionParser;
	private final AbstractEntityParser<ObjectInfo>		constructorParser;
	private final AbstractEntityParser<ObjectInfo>		customHierarchyParser;
	private final AbstractEntityParser<ObjectInfo> 		expressionParser;
	private final AbstractEntityParser<TypeToken<?>>	innerClassParser;
	private final AbstractEntityParser<ObjectInfo>		literalParser;
	private final AbstractEntityParser<ObjectInfo>		objectFieldParser;
	private final AbstractEntityParser<ObjectInfo>		objectMethodParser;
	private final AbstractEntityParser<ObjectInfo>		objectTailParser;
	private final AbstractEntityParser<ObjectInfo>		parenthesizedExpressionParser;
	private final AbstractEntityParser<ObjectInfo>		topLevelClassParser;
	private final AbstractEntityParser<ObjectInfo>		unaryPrefixOperatorParser;
	private final AbstractEntityParser<ObjectInfo>		variableParser;

	public ParserContext(ObjectInfo thisInfo, ParserSettings settings, EvaluationMode evaluationMode) {
		this.thisInfo = thisInfo;
		this.settings = settings;

		classDataProvider				= new ClassDataProvider(this);
		executableDataProvider			= new ExecutableDataProvider(this);
		fieldDataProvider				= new FieldDataProvider(this);
		inspectionDataProvider 			= new InspectionDataProvider(this);
		objectInfoProvider				= new ObjectInfoProvider(evaluationMode);
		objectTreeNodeDataProvider		= new ObjectTreeNodeDataProvider();
		operatorResultProvider 			= new OperatorResultProvider(this, evaluationMode);
		variableDataProvider			= new VariableDataProvider(settings.getVariablePool());

		castParser						= new CastParser(this, thisInfo);
		classFieldParser				= new ClassFieldParser(this, thisInfo);
		classMethodParser				= new ClassMethodParser(this, thisInfo);
		classTailParser					= new ClassTailParser(this, thisInfo);
		compoundExpressionParser		= createCompoundExpressionParser(OperatorResultProvider.MAX_BINARY_OPERATOR_PRECEDENCE_LEVEL);
		constructorParser				= new ConstructorParser(this, thisInfo);
		customHierarchyParser			= new CustomHierarchyParser(this, thisInfo);
		expressionParser 				= new ExpressionParser(this, thisInfo);
		innerClassParser				= new InnerClassParser(this, thisInfo);
		literalParser					= new LiteralParser(this, thisInfo);
		objectFieldParser				= new ObjectFieldParser(this, thisInfo);
		objectMethodParser				= new ObjectMethodParser(this, thisInfo);
		objectTailParser				= new ObjectTailParser(this, thisInfo);
		parenthesizedExpressionParser	= new ParenthesizedExpressionParser(this, thisInfo);
		topLevelClassParser				= new TopLevelClassParser(this, thisInfo);
		unaryPrefixOperatorParser		= new UnaryPrefixOperatorParser(this, thisInfo);
		variableParser					= new VariableParser(this, thisInfo);
	}

	public ObjectInfo getThisInfo() {
		return thisInfo;
	}

	public ParserSettings getSettings() {
		return settings;
	}

	/*
	 * Data Providers
	 */
	public ClassDataProvider getClassDataProvider() {
		return classDataProvider;
	}

	public ExecutableDataProvider getExecutableDataProvider() {
		return executableDataProvider;
	}

	public FieldDataProvider getFieldDataProvider() {
		return fieldDataProvider;
	}

	public InspectionDataProvider getInspectionDataProvider() {
		return inspectionDataProvider;
	}

	public ObjectInfoProvider getObjectInfoProvider() {
		return objectInfoProvider;
	}

	public ObjectTreeNodeDataProvider getObjectTreeNodeDataProvider() {
		return objectTreeNodeDataProvider;
	}

	public OperatorResultProvider getOperatorResultProvider() {
		return operatorResultProvider;
	}

	public VariableDataProvider getVariableDataProvider() {
		return variableDataProvider;
	}

	/*
	 * Parsers
	 */
	public AbstractEntityParser<ObjectInfo> getCastParser() { return castParser; }

	public AbstractEntityParser<TypeToken<?>> getClassFieldParser() {
		return classFieldParser;
	}

	public AbstractEntityParser<TypeToken<?>> getClassMethodParser() {
		return classMethodParser;
	}

	public AbstractEntityParser<TypeToken<?>> getClassTailParser() {
		return classTailParser;
	}

	public AbstractEntityParser<ObjectInfo> createCompoundExpressionParser(int maxOperatorPrecedenceLevelToConsider) {
		return new CompoundExpressionParser(this, thisInfo, maxOperatorPrecedenceLevelToConsider);
	}

	public AbstractEntityParser<ObjectInfo> getCompoundExpressionParser() {
		return compoundExpressionParser;
	}

	public AbstractEntityParser<ObjectInfo> getConstructorParser() {
		return constructorParser;
	}

	public AbstractEntityParser<ObjectInfo> getCustomHierarchyParser() {
		return customHierarchyParser;
	}

	public AbstractEntityParser<ObjectInfo> getExpressionParser() {
		return expressionParser;
	}

	public AbstractEntityParser<TypeToken<?>> getInnerClassParser() {
		return innerClassParser;
	}

	public AbstractEntityParser<ObjectInfo> getLiteralParser() {
		return literalParser;
	}

	public AbstractEntityParser<ObjectInfo> getObjectFieldParser() {
		return objectFieldParser;
	}

	public AbstractEntityParser<ObjectInfo> getObjectMethodParser() {
		return objectMethodParser;
	}

	public AbstractEntityParser<ObjectInfo> getObjectTailParser() {
		return objectTailParser;
	}

	public AbstractEntityParser<ObjectInfo> getParenthesizedExpressionParser() {
		return parenthesizedExpressionParser;
	}

	public AbstractEntityParser<ObjectInfo> getTopLevelClassParser() { return topLevelClassParser; }

	public AbstractEntityParser<ObjectInfo> getUnaryPrefixOperatorParser() {
		return unaryPrefixOperatorParser;
	}

	public AbstractEntityParser<ObjectInfo> getVariableParser() { return variableParser; }
}
