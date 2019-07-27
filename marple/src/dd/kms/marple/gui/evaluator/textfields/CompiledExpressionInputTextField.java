package dd.kms.marple.gui.evaluator.textfields;

import dd.kms.marple.InspectionContext;
import dd.kms.zenodot.CompiledExpression;
import dd.kms.zenodot.ExpressionCompiler;
import dd.kms.zenodot.ParseException;
import dd.kms.zenodot.Parsers;
import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;

import java.util.Map;
import java.util.Optional;

public class CompiledExpressionInputTextField extends AbstractExpressionInputTextField<CompiledExpression>
{
	private Class<?> thisClass = Object.class;

	public CompiledExpressionInputTextField(InspectionContext context) {
		super(context);
	}

	public void setThisClass(Class<?> thisClass) {
		this.thisClass = thisClass;
	}

	@Override
	Map<CompletionSuggestion, MatchRating> suggestCodeCompletion(String text, int caretPosition) throws ParseException {
		ExpressionCompiler compiler = createCompiler(text);
		return compiler.suggestCodeCompletion(caretPosition);
	}

	@Override
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String text, int caretPosition) throws ParseException  {
		ExpressionCompiler compiler = createCompiler(text);
		return compiler.getExecutableArgumentInfo(caretPosition);
	}

	@Override
	CompiledExpression evaluate(String text) throws ParseException {
		ExpressionCompiler compiler = createCompiler(text);
		return compiler.compile();

	}

	private ExpressionCompiler createCompiler(String text) {
		return Parsers.createExpressionCompiler(text, getParserSettings(), thisClass);
	}
}
