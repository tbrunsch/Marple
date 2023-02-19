package dd.kms.marple.impl.gui.evaluator.textfields;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.marple.impl.evaluator.ExpressionEvaluators;
import dd.kms.zenodot.api.*;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;

import java.util.List;
import java.util.Optional;

public class CompiledExpressionInputTextField extends AbstractExpressionInputTextField<CompiledExpression>
{
	private Class<?>	thisType	= Object.class;

	public CompiledExpressionInputTextField(InspectionContext context) {
		super(context);
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
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) throws ParseException  {
		ExpressionParser parser = createParser();
		return parser.getExecutableArgumentInfo(text, caretPosition, thisType);
	}

	@Override
	CompiledExpression evaluate(String text) throws ParseException {
		ExpressionParser parser = createParser();
		return parser.compile(text, thisType);
	}

	private ExpressionParser createParser() {
		List<Variable> variables = getVariables();
		Variables variableCollection = ExpressionEvaluators.toVariableCollection(variables, true);
		return Parsers.createExpressionParserBuilder(getParserSettings())
			.variables(variableCollection)
			.createExpressionParser();
	}
}
