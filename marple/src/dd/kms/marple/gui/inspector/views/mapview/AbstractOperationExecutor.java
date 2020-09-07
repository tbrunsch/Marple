package dd.kms.marple.gui.inspector.views.mapview;

import com.google.common.base.Preconditions;
import dd.kms.marple.InspectionContext;
import dd.kms.marple.gui.inspector.views.mapview.settings.OperationSettings;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.util.Map;
import java.util.function.Consumer;

abstract class AbstractOperationExecutor<T extends OperationSettings>
{
	final Map<?, ?>					map;
	private final TypeInfo			commonKeyType;
	private final TypeInfo			commonValueType;
	final T							settings;
	final InspectionContext			inspectionContext;

	private Consumer<ObjectInfo>	resultConsumer;
	private Consumer<String>		textConsumer;

	AbstractOperationExecutor(Map<?, ?> map, TypeInfo commonKeyType, TypeInfo commonValueType, T settings, InspectionContext inspectionContext) {
		this.map = map;
		this.commonKeyType = commonKeyType;
		this.commonValueType = commonValueType;
		this.settings = settings;
		this.inspectionContext = inspectionContext;
	}

	abstract void execute() throws Exception;

	void setResultConsumer(Consumer<ObjectInfo> resultConsumer) {
		this.resultConsumer = resultConsumer;
	}

	void setTextConsumer(Consumer<String> textConsumer) {
		this.textConsumer = textConsumer;
	}

	void displayResult(ObjectInfo result) {
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

	CompiledExpression compileKeyExpression(String expression) throws ParseException {
		return compile(expression, commonKeyType);
	}

	CompiledExpression compileValueExpression(String expression) throws ParseException {
		return compile(expression, commonValueType);
	}

	private CompiledExpression compile(String expression, TypeInfo type) throws ParseException {
		ExpressionParser parser = Parsers.createExpressionParser(inspectionContext.getEvaluator().getParserSettings());
		ObjectInfo thisValue = InfoProvider.createObjectInfo(InfoProvider.INDETERMINATE_VALUE, type);
		return parser.compile(expression, thisValue);
	}
}
