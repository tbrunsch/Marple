package dd.kms.marple.impl.gui.evaluator.textfields;

import dd.kms.marple.api.InspectionContext;
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

public class ExpressionInputTextField extends AbstractExpressionInputTextField<ObjectInfo>
{
	private ObjectInfo	thisValue = InfoProvider.NULL_LITERAL;

	public ExpressionInputTextField(InspectionContext context) {
		super(context);
	}

	public void setThisValue(ObjectInfo thisValue) {
		this.thisValue = thisValue;
	}

	@Override
	List<CodeCompletion> suggestCodeCompletion(String text, int caretPosition) throws ParseException {
		ExpressionParser parser = createParser();
		return parser.getCompletions(text, caretPosition, thisValue);
	}

	@Override
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) throws ParseException  {
		ExpressionParser parser = createParser();
		return parser.getExecutableArgumentInfo(text, caretPosition, thisValue);
	}

	@Override
	ObjectInfo evaluate(String text) throws ParseException {
		ExpressionParser parser = createParser();
		ObjectInfo result = parser.evaluate(text, thisValue);
		Object resultObject = result.getObject();
		if (resultObject != null) {
			Class<?> resultObjectClass = resultObject.getClass();
			try {
				TypeInfo resultObjectType = result.getDeclaredType().getSubtype(resultObjectClass);
				return InfoProvider.createObjectInfo(resultObject, resultObjectType, result.getValueSetter());
			} catch (Throwable ignored) {
				/* fall through to default case */
			}
		}
		return result;
	}

	private ExpressionParser createParser() {
		return Parsers.createExpressionParser(getParserSettings());
	}
}
