package dd.kms.marple.impl.gui.evaluator.textfields;

import dd.kms.marple.api.InspectionContext;
import dd.kms.zenodot.api.ParseException;
import dd.kms.zenodot.api.result.CodeCompletion;

import java.util.List;

abstract class AbstractExpressionInputTextField<T> extends AbstractInputTextField<T>
{
	public AbstractExpressionInputTextField(InspectionContext context) {
		super(context);
	}

	public void setExpression(String expression) {
		setText(expression);
		setCaretPosition(expression == null ? 0 : expression.length());
	}

	public String getExpression() {
		return getText();
	}

	abstract List<CodeCompletion> suggestCodeCompletion(String text, int caretPosition) throws ParseException;

	@Override
	List<CodeCompletion> doProvideCompletions(String text, int caretPosition) throws ParseException {
		List<CodeCompletion> completions = suggestCodeCompletion(text, caretPosition);
		return filterCompletions(completions);
	}
}
