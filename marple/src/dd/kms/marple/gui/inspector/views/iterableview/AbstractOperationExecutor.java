package dd.kms.marple.gui.inspector.views.iterableview;

import com.google.common.base.Preconditions;
import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.util.function.Consumer;

abstract class AbstractOperationExecutor
{
	final Iterable<?>			iterable;
	private final TypeInfo		commonElementType;
	final InspectionContext		inspectionContext;

	private Consumer<Object>	resultConsumer;
	private Consumer<String>	textConsumer;

	AbstractOperationExecutor(Iterable<?> iterable, TypeInfo commonElementType, InspectionContext inspectionContext) {
		this.iterable = iterable;
		this.commonElementType = commonElementType;
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
		return new Exception("Error evaluating exception for '" + inspectionContext.getDisplayText(InfoProvider.createObjectInfo(element)) + "'", e);
	}

	CompiledExpression compile(String expression) throws ParseException {
		ExpressionParser parser = Parsers.createExpressionParser(inspectionContext.getEvaluator().getParserSettings());
		ObjectInfo commonElementRepresentative = InfoProvider.createObjectInfo(InfoProvider.INDETERMINATE_VALUE, commonElementType);
		return parser.compile(expression, commonElementRepresentative);
	}
}
