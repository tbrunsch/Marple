package dd.kms.marple.gui.evaluator.textfields;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.ExpressionParser;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.Parsers;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.utils.wrappers.InfoProvider;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.Map;
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
	Map<CompletionSuggestion, MatchRating> suggestCodeCompletion(String text, int caretPosition) throws ParseException {
		ExpressionParser parser = createParser(text);
		return parser.suggestCodeCompletion(caretPosition);
	}

	@Override
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) throws ParseException  {
		ExpressionParser parser = createParser(text);
		return parser.getExecutableArgumentInfo(caretPosition);
	}

	@Override
	ObjectInfo evaluate(String text) throws ParseException {
		ExpressionParser parser = createParser(text);
		return parser.evaluate();

	}

	private ExpressionParser createParser(String text) {
		return Parsers.createExpressionParser(text, getParserSettings(), thisValue);
	}
}
