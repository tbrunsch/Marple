package dd.kms.marple.impl.gui.evaluator.textfields;

import dd.kms.marple.api.InspectionContext;
import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.Parsers;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;

import java.util.List;
import java.util.Optional;

public class ExpressionInputTextField extends AbstractExpressionInputTextField<Object>
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
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) throws ParseException  {
		ExpressionParser parser = createParser();
		return parser.getExecutableArgumentInfo(text, caretPosition, thisValue);
	}

	@Override
	Object evaluate(String text) throws ParseException {
		ExpressionParser parser = createParser();
		return parser.evaluate(text, thisValue);
	}

	private ExpressionParser createParser() {
		return Parsers.createExpressionParser(getParserSettings(), getVariables());
	}
}
