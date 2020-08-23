package dd.kms.marple.gui.evaluator.textfields;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.api.CompiledExpression;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

import java.util.List;
import java.util.Optional;

public class CompiledExpressionInputTextField extends AbstractExpressionInputTextField<CompiledExpression>
{
	private TypeInfo thisType = InfoProvider.createTypeInfo(Object.class);

	public CompiledExpressionInputTextField(InspectionContext context) {
		super(context);
	}

	public void setThisType(TypeInfo thisType) {
		this.thisType = thisType;
	}

	@Override
	List<CodeCompletion> suggestCodeCompletion(String text, int caretPosition) throws ParseException {
		ExpressionParser parser = createParser();
		ObjectInfo thisValue = getThisValue();
		return parser.getCompletions(text, caretPosition, thisValue);
	}

	@Override
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) throws ParseException  {
		ExpressionParser parser = createParser();
		ObjectInfo thisValue = getThisValue();
		return parser.getExecutableArgumentInfo(text, caretPosition, thisValue);
	}

	@Override
	CompiledExpression evaluate(String text) throws ParseException {
		ExpressionParser parser = createParser();
		ObjectInfo thisValue = getThisValue();
		return parser.compile(text, thisValue);
	}

	private ExpressionParser createParser() {
		return Parsers.createExpressionParser(getParserSettings());
	}

	private ObjectInfo getThisValue() {
		return InfoProvider.createObjectInfo(InfoProvider.INDETERMINATE_VALUE, thisType);
	}
}
