package dd.kms.marple.impl.gui.inspector.views.iterableview;

import com.google.common.base.Preconditions;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.marple.impl.evaluator.ExpressionEvaluators;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.OperationSettings;
import dd.kms.zenodot.api.*;

import java.util.List;
import java.util.function.Consumer;

abstract class AbstractOperationExecutor<T extends OperationSettings>
{
	final Object				object;
	final Iterable<?>			iterableView;
	final Class<?>				commonElementType;
	final T						settings;
	final InspectionContext		context;

	private Consumer<Object>	resultConsumer;
	private Consumer<String>	textConsumer;

	AbstractOperationExecutor(Object object, Iterable<?> iterableView, Class<?> commonElementType, T settings, InspectionContext context) {
		this.object = object;
		this.iterableView = iterableView;
		this.commonElementType = commonElementType;
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

	<F> CompiledLambdaExpression<F> compile(String expression, Class<F> functionalInterface, Class<?>... parameterTypes) throws ParseException {
		ExpressionEvaluator evaluator = context.getEvaluator();
		List<Variable> variables = evaluator.getVariables();
		Variables variableCollection = ExpressionEvaluators.toVariableCollection(variables, true);
		LambdaExpressionParser<F> parser = Parsers.createExpressionParserBuilder(evaluator.getParserSettings())
			.variables(variableCollection)
			.createLambdaParser(functionalInterface, parameterTypes);
		return parser.compile(expression, object);
	}
}
