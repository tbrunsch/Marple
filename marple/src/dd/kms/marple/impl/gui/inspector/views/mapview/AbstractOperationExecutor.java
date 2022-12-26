package dd.kms.marple.impl.gui.inspector.views.mapview;

import com.google.common.base.Preconditions;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.marple.impl.evaluator.ExpressionEvaluators;
import dd.kms.marple.impl.gui.inspector.views.mapview.settings.OperationSettings;
import dd.kms.zenodot.api.*;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

abstract class AbstractOperationExecutor<T extends OperationSettings>
{
	final Map<?, ?>				map;
	private final Class<?>		commonKeyType;
	private final Class<?>		commonValueType;
	final T						settings;
	final InspectionContext		context;

	private Consumer<Object>	resultConsumer;
	private Consumer<String>	textConsumer;

	AbstractOperationExecutor(Map<?, ?> map, Class<?> commonKeyType, Class<?> commonValueType, T settings, InspectionContext context) {
		this.map = map;
		this.commonKeyType = commonKeyType;
		this.commonValueType = commonValueType;
		this.settings = settings;
		this.context = context;
	}

	abstract void execute() throws Exception;

	void setResultConsumer(Consumer<Object> resultConsumer) {
		this.resultConsumer = resultConsumer;
	}

	void setTextConsumer(Consumer<String> textConsumer) {
		this.textConsumer = textConsumer;
	}

	void displayResult(Object result) {
		Preconditions.checkNotNull(resultConsumer);
		resultConsumer.accept(result);
	}

	void displayText(String text) {
		Preconditions.checkNotNull(textConsumer);
		textConsumer.accept(text);
	}

	Exception wrapEvaluationException(Exception e, Object element) {
		return new Exception("Error evaluating exception for '" + context.getDisplayText(element) + "'", e);
	}

	CompiledExpression compileKeyExpression(String expression) throws ParseException {
		return compile(expression, commonKeyType);
	}

	CompiledExpression compileValueExpression(String expression) throws ParseException {
		return compile(expression, commonValueType);
	}

	private CompiledExpression compile(String expression, Class<?> type) throws ParseException {
		ExpressionEvaluator evaluator = context.getEvaluator();
		List<Variable> variables = evaluator.getVariables();
		Variables variableCollection = ExpressionEvaluators.toVariableCollection(variables, true);
		ExpressionParser parser = Parsers.createExpressionParser(evaluator.getParserSettings(), variableCollection);
		return parser.compile(expression, type);
	}
}
