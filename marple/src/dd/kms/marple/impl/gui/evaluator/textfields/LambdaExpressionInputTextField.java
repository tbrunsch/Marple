package dd.kms.marple.impl.gui.evaluator.textfields;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.marple.impl.evaluator.ExpressionEvaluators;
import dd.kms.zenodot.api.*;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;

import java.util.List;
import java.util.Optional;

public class LambdaExpressionInputTextField extends AbstractExpressionInputTextField<CompiledLambdaExpression<?>>
{
	private final Class<?>	functionalInterface;
	private Class<?>[] 		parameterTypes			= new Class[0];
	private Class<?>		thisType				= Object.class;

	public LambdaExpressionInputTextField(Class<?> functionalInterface, InspectionContext context) {
		super(context);
		this.functionalInterface = functionalInterface;
	}

	public void setParameterTypes(Class<?>... parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public void setThisType(Class<?> thisType) {
		this.thisType = thisType;
	}

	@Override
	List<CodeCompletion> suggestCodeCompletion(String text, int caretPosition) throws ParseException {
		ExpressionParser parser = createParser();
		return parser.getCompletions(text, caretPosition, thisType);
	}

	@Override
	public Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) throws ParseException  {
		ExpressionParser parser = createParser();
		return parser.getExecutableArgumentInfo(text, caretPosition, thisType);
	}

	@Override
	CompiledLambdaExpression<?> evaluate(String text) throws ParseException {
		LambdaExpressionParser<?> parser = createParser();
		return parser.compile(text, thisType);
	}

	private LambdaExpressionParser<?> createParser() {
		List<Variable> variables = getVariables();
		Variables variableCollection = ExpressionEvaluators.toVariableCollection(variables, true);
		return Parsers.createExpressionParserBuilder(getParserSettings())
			.variables(variableCollection)
			.createLambdaParser(functionalInterface, parameterTypes);
	}
}
