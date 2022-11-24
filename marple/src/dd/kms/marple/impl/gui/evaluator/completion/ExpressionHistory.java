package dd.kms.marple.impl.gui.evaluator.completion;

import java.util.ArrayList;
import java.util.List;

class ExpressionHistory
{
	static final int	PREVIOUS	= -1;
	static final int	NEXT		= 1;

	private final List<String>	evaluatedExpressions	= new ArrayList<>();
	private int					lookupIndex				= 0;

	/**
	 * Adds an expression as most recent expression to the history and resets the lookup index.
	 */
	void addExpression(String expression) {
		evaluatedExpressions.add(expression);
		lookupIndex = evaluatedExpressions.size();
	}

	/**
	 * @param searchDirection The direction to search. Should be {@link #PREVIOUS} or {@link #NEXT}.
	 * @return The previous or next expression in the history of evaluated expressions
	 */
	String lookup(int searchDirection) {
		lookupIndex = Math.min(Math.max(0, lookupIndex + searchDirection), evaluatedExpressions.size() - 1);
		return lookupIndex >= 0 ? evaluatedExpressions.get(lookupIndex) : null;
	}
}
