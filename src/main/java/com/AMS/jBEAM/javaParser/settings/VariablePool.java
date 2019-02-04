package com.AMS.jBEAM.javaParser.settings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.lang.ref.WeakReference;
import java.util.List;

public class VariablePool
{
	private final ImmutableMap<String, ValueData> variables;

	VariablePool(ImmutableMap<String, ValueData> variables) {
		this.variables = variables;
	}

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

	static class ValueData
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
