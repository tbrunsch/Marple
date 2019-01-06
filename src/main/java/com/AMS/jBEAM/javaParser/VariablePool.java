package com.AMS.jBEAM.javaParser;

import com.google.common.collect.ImmutableList;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VariablePool implements VariablePoolIF
{
	private final Map<String, ValueData> variables = new LinkedHashMap<>();

	@Override
	public void addVariable(Variable variable) {
		variables.put(variable.getName(), new ValueData(variable.getValue(), variable.isUseHardReferenceInPool()));
	}

	@Override
	public List<Variable> getVariables() {
		ImmutableList.Builder<Variable> builder = ImmutableList.builder();
		for (String name : variables.keySet()) {
			ValueData valueData = variables.get(name);
			Object value = valueData.getValue();
			if (!valueData.isGarbageCollected()) {
				builder.add(new Variable(name, value, valueData.isUseHardReference()));
			}
		}
		return builder.build();
	}

	private static class ValueData
	{
		private final WeakReference<Object> weakValueReference;	// always set
		private final Object				hardValueReference;	// only set if user wants to save variables from being garbage collected
		private final boolean				valueIsNull;
		private final boolean				useHardReference;

		ValueData(Object value, boolean useHardReference) {
			weakValueReference = new WeakReference<>(value);
			hardValueReference = useHardReference ? value : null;
			valueIsNull = value == null;
			this.useHardReference = useHardReference;
		}

		boolean isUseHardReference() {
			return useHardReference;
		}

		Object getValue() {
			return weakValueReference.get();
		}

		boolean isGarbageCollected() {
			return getValue() == null && !valueIsNull;
		}
	}
}
