package dd.kms.marple.impl.gui.evaluator.textfields;

import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.marple.api.gui.Disposable;
import dd.kms.marple.impl.evaluator.ExpressionEvaluators;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;

import java.util.*;

public class ExpressionInputTextField extends AbstractExpressionInputTextField<Object> implements Disposable
{
	private Object	thisValue	= null;

	public ExpressionInputTextField(InspectionContext context) {
		super(context);
	}

	public void setThisValue(Object thisValue) {
		this.thisValue = thisValue;
	}

	@Override
	List<CodeCompletion> suggestCodeCompletion(String text, int caretPosition) throws ParseException {
		ExpressionParser parser = createParser();
		return parser.getCompletions(text, caretPosition, thisValue);
	}

	@Override
	public Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) throws ParseException  {
		ExpressionParser parser = createParser();
		return parser.getExecutableArgumentInfo(text, caretPosition, thisValue);
	}

	@Override
	Object evaluate(String text) throws ParseException {
		List<Variable> oldVariables = getVariables();
		Variables variableCollection = ExpressionEvaluators.toVariableCollection(oldVariables, false);

		ExpressionParser parser = createParser(variableCollection);
		Object result = parser.evaluate(text, thisValue);

		Collection<String> variableNames = variableCollection.getNames();
		Map<String, Variable> variablesByName = new LinkedHashMap<>();
		for (Variable oldVariable : oldVariables) {
			variablesByName.put(oldVariable.getName(), oldVariable);
		}

		boolean changedVariableSet = false;
		for (String variableName : variableNames) {
			Variable oldVariable = variablesByName.get(variableName);
			Object value = variableCollection.getValue(variableName);
			if (oldVariable != null && Objects.equals(oldVariable.getType(), value)) {
				continue;
			}
			Class<?> type = variableCollection.getType(variableName);
			Variable newVariable = Variable.create(variableName, type, value, false, true);
			variablesByName.put(variableName, newVariable);
			changedVariableSet = true;
		}

		if (changedVariableSet) {
			setVariables(new ArrayList<>(variablesByName.values()));
		}

		return result;
	}

	private ExpressionParser createParser() {
		Variables variables = ExpressionEvaluators.toVariableCollection(getVariables(), false);
		return createParser(variables);
	}

	private ExpressionParser createParser(Variables variables) {
		return Parsers.createExpressionParserBuilder(getParserSettings())
			.variables(variables)
			.createExpressionParser();
	}

	@Override
	public void dispose() {
		thisValue = null;
	}
}
