package dd.kms.marple.impl.gui.inspector.views.iterableview;

import com.google.common.base.Preconditions;
import dd.kms.marple.api.InspectionContext;
import dd.kms.marple.impl.gui.inspector.views.iterableview.settings.OperationSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;

import java.util.function.Consumer;

abstract class AbstractOperationExecutor<T extends OperationSettings>
{
	final Iterable<?>			iterable;
	private final Class<?>		commonElementType;
	final T						settings;
	final InspectionContext		context;

	private Consumer<Object>	resultConsumer;
	private Consumer<String>	textConsumer;

	AbstractOperationExecutor(Iterable<?> iterable, Class<?> commonElementType, T settings, InspectionContext context) {
		this.iterable = iterable;
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

	CompiledExpression compile(String expression) throws ParseException {
		ExpressionParser parser = Parsers.createExpressionParser(context.getEvaluator().getParserSettings());
		return parser.compile(expression, commonElementType);
	}
}
