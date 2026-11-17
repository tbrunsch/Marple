package dd.kms.marple.impl.aiagent;

import com.google.common.collect.ImmutableList;
import dd.kms.marple.api.evaluator.ExpressionEvaluator;
import dd.kms.marple.api.evaluator.Variable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The variables the agent has created in one conversation. They belong to the conversation:
 * <ul>
 *     <li>
 *         While the conversation is shown, the variables are attached, i.e., they are part of the
 *         evaluator's variables.
 *     </li>
 *     <li>
 *         When the view is disposed (e.g., when the user navigates to another object), they are
 *         detached, i.e., removed from the evaluator, but kept in this object. Since the view settings
 *         reference this object, the variables are attached again when the user navigates back. If the
 *         conversation is dropped from the history, then the variables are garbage collected with it.
 *     </li>
 * </ul>
 * A variable is only considered to be the agent's as long as the evaluator contains a variable with
 * the same name and the same value. If the user deletes or replaces it, then it is not tracked anymore.
 * The agent may only overwrite and delete its own variables.<br>
 * <br>
 * While attached, this object listens to the evaluator's variables. Hence, the listeners of this object
 * are notified about all changes, including changes the user makes in Marple. An object that is not
 * used anymore must be detached. Otherwise, the evaluator keeps it alive.<br>
 * <br>
 * All methods must be called on the event dispatch thread. The evaluator's variables must also only be
 * changed on the event dispatch thread.
 */
public final class AgentVariables
{
	private final ExpressionEvaluator		evaluator;
	private final Map<String, Variable>		agentVariablesByName	= new LinkedHashMap<>();
	private final List<Runnable>			listeners				= new ArrayList<>();
	private final Runnable					evaluatorListener		= this::onEvaluatorVariablesChanged;
	private boolean							attached				= true;
	/**
	 * {@code true} while this object changes the evaluator's variables. These changes are consistent
	 * with {@link #agentVariablesByName} and must not be handled by {@link #onEvaluatorVariablesChanged()}.
	 */
	private boolean							changingEvaluator		= false;

	public AgentVariables(ExpressionEvaluator evaluator) {
		this.evaluator = evaluator;
		evaluator.addVariablesListener(evaluatorListener);
	}

	/**
	 * Listeners must be removed when they are not needed anymore: This object is referenced by the
	 * view settings in the inspection history and would keep them alive.
	 */
	public void addChangeListener(Runnable listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(Runnable listener) {
		listeners.remove(listener);
	}

	/**
	 * Returns the agent's variables that still exist in the evaluator.
	 */
	public List<Variable> getVariables() {
		return ImmutableList.copyOf(agentVariablesByName.values());
	}

	/**
	 * Returns {@code true} while the conversation is active, i.e., while its variables are part of the evaluator.
	 */
	public boolean isAttached() {
		return attached;
	}

	public boolean isAgentVariable(String name) {
		return agentVariablesByName.containsKey(name);
	}

	/**
	 * Returns {@code true} if the agent may store a value under the specified name, i.e., if there
	 * is no variable with that name or if it is one of the agent's variables.
	 */
	public boolean canStore(String name) {
		return attached && (findEvaluatorVariable(name) == null || isAgentVariable(name));
	}

	/**
	 * Stores the value in a variable of the agent.
	 *
	 * @throws IllegalStateException if the agent must not store a value under this name (see {@link #canStore(String)})
	 */
	public void store(String name, @Nullable Object value) {
		if (!canStore(name)) {
			throw new IllegalStateException(attached
				? "The variable '" + name + "' belongs to the user"
				: "The conversation is not active anymore");
		}
		Class<?> type = value == null ? Object.class : value.getClass();
		Variable variable = Variable.create(name, type, value, false, true);
		agentVariablesByName.put(name, variable);
		changeEvaluator(() -> evaluator.addVariable(variable));
		fireChanged();
	}

	/**
	 * Deletes the specified variable if it is one of the agent's variables.
	 *
	 * @return {@code true} if the variable has been deleted
	 */
	public boolean delete(String name) {
		if (!attached || !isAgentVariable(name)) {
			return false;
		}
		agentVariablesByName.remove(name);
		changeEvaluator(() -> evaluator.removeVariable(name));
		fireChanged();
		return true;
	}

	/**
	 * Deletes all variables of the agent.
	 */
	public void deleteAll() {
		if (agentVariablesByName.isEmpty()) {
			return;
		}
		if (attached) {
			changeEvaluator(() -> agentVariablesByName.keySet().forEach(evaluator::removeVariable));
		}
		agentVariablesByName.clear();
		fireChanged();
	}

	/**
	 * Hands the variable over to the user: It remains in the evaluator, but it is not tracked
	 * as the agent's variable anymore. In particular, it is not removed when the conversation ends.
	 */
	public void keep(String name) {
		if (agentVariablesByName.remove(name) != null) {
			fireChanged();
		}
	}

	/**
	 * Removes the agent's variables from the evaluator, but keeps them for {@link #attach()}.
	 * While detached, no variables can be stored and this object does not listen to the evaluator.
	 */
	public void detach() {
		if (!attached) {
			return;
		}
		evaluator.removeVariablesListener(evaluatorListener);
		changeEvaluator(() -> agentVariablesByName.keySet().forEach(evaluator::removeVariable));
		attached = false;
		fireChanged();
	}

	/**
	 * Adds the agent's variables to the evaluator again. Variables whose names have been
	 * used by the user in the meantime are dropped: The user's variables take precedence.
	 */
	public void attach() {
		if (attached) {
			return;
		}
		attached = true;
		Iterator<Variable> iter = agentVariablesByName.values().iterator();
		while (iter.hasNext()) {
			Variable variable = iter.next();
			if (findEvaluatorVariable(variable.getName()) != null) {
				iter.remove();
			} else {
				changeEvaluator(() -> evaluator.addVariable(variable));
			}
		}
		evaluator.addVariablesListener(evaluatorListener);
		fireChanged();
	}

	private void changeEvaluator(Runnable change) {
		changingEvaluator = true;
		try {
			change.run();
		} finally {
			changingEvaluator = false;
		}
	}

	/**
	 * Stops tracking variables that the user has deleted or replaced and notifies the listeners if this changed anything.
	 */
	private void onEvaluatorVariablesChanged() {
		if (changingEvaluator || !attached) {
			return;
		}
		boolean changed = agentVariablesByName.values().removeIf(variable -> {
			Variable evaluatorVariable = findEvaluatorVariable(variable.getName());
			return evaluatorVariable == null || evaluatorVariable.getValue() != variable.getValue();
		});
		if (changed) {
			fireChanged();
		}
	}

	private void fireChanged() {
		// iterate over a copy so listeners may add or remove listeners without causing a ConcurrentModificationException
		for (Runnable listener : new ArrayList<>(listeners)) {
			listener.run();
		}
	}

	@Nullable
	private Variable findEvaluatorVariable(String name) {
		for (Variable variable : evaluator.getVariables()) {
			if (variable.getName().equals(name)) {
				return variable;
			}
		}
		return null;
	}
}
