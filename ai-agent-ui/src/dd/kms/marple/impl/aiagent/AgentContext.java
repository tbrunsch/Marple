package dd.kms.marple.impl.aiagent;

import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.zenodot.api.settings.EvaluationMode;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The evaluator of one conversation. It is separated from the user's evaluator:
 * <ul>
 *     <li>
 *         It starts with a copy of the user's parser settings and variables. Afterwards, the user and the agent
 *         do not influence each other: The agent may overwrite and delete all variables in its evaluator and change
 *         its settings, and variables the user creates later are not visible to the agent.
 *     </li>
 *     <li>
 *         Expressions are always evaluated with static typing, independent of the user's evaluation mode.
 *     </li>
 *     <li>
 *         The variables the agent has created are only added to the user's evaluator if the user keeps them
 *         (see {@link #keep(String, ExpressionEvaluator)}).
 *     </li>
 * </ul>
 * The context belongs to the conversation: It is referenced by the view settings, so the agent's variables and
 * settings are restored when the user navigates back. It is garbage collected with the last snapshot.<br>
 * <br>
 * All methods must be called on the event dispatch thread.
 */
public class AgentContext
{
	private final ExpressionEvaluator	evaluator		= ExpressionEvaluator.create();
	/**
	 * The names of the variables the agent has created and the user has not kept yet
	 */
	private final Set<String>			createdNames	= new LinkedHashSet<>();
	private final List<Runnable>		listeners		= new ArrayList<>();

	public AgentContext(ExpressionEvaluator userEvaluator) {
		evaluator.setParserSettings(userEvaluator.getParserSettings().builder().evaluationMode(EvaluationMode.STATIC_TYPING).build());
		evaluator.setVariables(userEvaluator.getVariables());
	}

	public ExpressionEvaluator getEvaluator() {
		return evaluator;
	}

	/**
	 * Listeners are notified when the variables the agent has created change. They must be removed when they are
	 * not needed anymore: This object is referenced by the view settings in the inspection history and would keep
	 * them alive.
	 */
	public void addChangeListener(Runnable listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(Runnable listener) {
		listeners.remove(listener);
	}

	/**
	 * Returns the variables the agent has created and the user has not kept yet.
	 */
	public List<Variable> getVariables() {
		return evaluator.getVariables().stream()
			.filter(variable -> isAgentVariable(variable.getName()))
			.collect(Collectors.toList());
	}

	public boolean isAgentVariable(String name) {
		return createdNames.contains(name);
	}

	/**
	 * Stores the value in a variable of the agent. An existing variable with the same name is replaced.
	 */
	public void store(String name, @Nullable Object value) {
		Class<?> type = value == null ? Object.class : value.getClass();
		evaluator.addVariable(Variable.create(name, type, value, false, true));
		createdNames.add(name);
		fireChanged();
	}

	/**
	 * Deletes the specified variable from the agent's evaluator.
	 *
	 * @return {@code true} if the variable has existed
	 */
	public boolean delete(String name) {
		boolean deleted = evaluator.removeVariable(name);
		if (createdNames.remove(name)) {
			fireChanged();
		}
		return deleted;
	}

	/**
	 * Deletes all variables the agent has created and the user has not kept.
	 */
	public void deleteAll() {
		if (createdNames.isEmpty()) {
			return;
		}
		createdNames.forEach(evaluator::removeVariable);
		createdNames.clear();
		fireChanged();
	}

	/**
	 * Hands the variable over to the user: It is added to the user's evaluator, replacing a variable with the same
	 * name, and it is not listed as a variable the agent has created anymore.
	 */
	public void keep(String name, ExpressionEvaluator userEvaluator) {
		Variable variable = findVariable(evaluator, name);
		if (variable == null || !createdNames.remove(name)) {
			return;
		}
		userEvaluator.addVariable(variable);
		fireChanged();
	}

	@Nullable
	static Variable findVariable(ExpressionEvaluator evaluator, String name) {
		for (Variable variable : evaluator.getVariables()) {
			if (variable.getName().equals(name)) {
				return variable;
			}
		}
		return null;
	}

	private void fireChanged() {
		// iterate over a copy so listeners may add or remove listeners without causing a ConcurrentModificationException
		for (Runnable listener : new ArrayList<>(listeners)) {
			listener.run();
		}
	}
}
