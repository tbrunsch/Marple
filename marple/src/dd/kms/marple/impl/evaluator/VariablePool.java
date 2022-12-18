package dd.kms.marple.impl.evaluator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dd.kms.marple.api.evaluator.Variable;
import dd.kms.zenodot.api.settings.ParserSettingsUtils;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Manages a collection of variables. If requested (see {@link Variable#isUseHardReference()}),
 * then the pool will not reference certain variable values by hard references to allow garbage
 * collection.
 */
class VariablePool
{
	private final ImmutableMap<String, ValueData> variables;

	VariablePool(List<Variable> variables) {
		ImmutableMap.Builder<String, ValueData> variablesBuilder = ImmutableMap.builder();
		for (Variable variable : variables) {
			variablesBuilder.put(variable.getName(), new ValueData(variable.getType(), variable.getValue(), variable.isFinal(), variable.isUseHardReference()));
		}
		this.variables = variablesBuilder.build();
	}

	List<Variable> getVariables() {
		ImmutableList.Builder<Variable> builder = ImmutableList.builder();
		for (String name : variables.keySet()) {
			ValueData valueData = variables.get(name);
			if (!valueData.isGarbageCollected()) {
				Class<?> type = valueData.getType();
				Object value = valueData.getValue();
				boolean isFinal = valueData.isFinal();
				boolean useHardReference = valueData.isUseHardReference();
				builder.add(Variable.create(name, type, value, isFinal, useHardReference));
			}
		}
		return builder.build();
	}

	private static class ValueData
	{
		private Class<?>					type;
		private final WeakReference<Object>	weakValueReference;
		private final Object				hardValueReference;	// only set if user wants to save variables from being garbage collected
		private final boolean				valueIsNull;
		private final boolean				isFinal;
		private final boolean				useHardReference;

		ValueData(Class<?> type, Object value, boolean isFinal, boolean useHardReference) {
			this.type = type;
			weakValueReference = new WeakReference<>(value);
			hardValueReference = useHardReference ? value : null;
			valueIsNull = value == null;
			this.isFinal = isFinal;
			this.useHardReference = useHardReference;
		}

		Class<?> getType() {
			return type;
		}

		Object getValue() {
			return weakValueReference.get();
		}

		boolean isFinal() {
			return isFinal;
		}

		boolean isUseHardReference() {
			return useHardReference;
		}

		boolean isGarbageCollected() {
			return weakValueReference.get() == null && !valueIsNull;
		}
	}
}
