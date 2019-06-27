package dd.kms.marple.gui.inspector.views.iterableview;

import com.google.common.base.Preconditions;
import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.CompiledExpression;
import dd.kms.zenodot.ExpressionCompiler;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.Parsers;

import java.util.function.Consumer;

abstract class AbstractOperationExecutor
{
	final Iterable<?>			iterable;
	private final Class<?>		commonElementClass;
	final InspectionContext		inspectionContext;

	private Consumer<Object>	resultConsumer;
	private Consumer<String>	textConsumer;

	AbstractOperationExecutor(Iterable<?> iterable, Class<?> commonElementClass, InspectionContext inspectionContext) {
		this.iterable = iterable;
		this.commonElementClass = commonElementClass;
		this.inspectionContext = inspectionContext;
	}

	abstract void execute(String expression, OperationResultType resultType) throws Exception;

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
		return new Exception("Error evaluating exception for '" + inspectionContext.getDisplayText(element) + "'", e);
	}

	CompiledExpression compile(String expression) throws ParseException {
		ExpressionCompiler compiler = Parsers.createExpressionCompiler(expression, inspectionContext.getEvaluator().getParserSettings(), commonElementClass);
		return compiler.compile();
	}
}
