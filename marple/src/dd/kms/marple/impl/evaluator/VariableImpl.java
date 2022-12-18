package dd.kms.marple.impl.evaluator;

import dd.kms.marple.api.evaluator.Variable;

public class VariableImpl implements Variable
{
	private final String	name;
	private final Class<?>	type;
	private final Object	value;
	private final boolean	isFinal;
	private final boolean	useHardReference;

	public VariableImpl(String name, Class<?> type, Object value, boolean isFinal, boolean useHardReference) {
		this.name = name;
		this.type = type;
		this.value = value;
		this.isFinal = isFinal;
		this.useHardReference = useHardReference;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public boolean isFinal() {
		return isFinal;
	}

	@Override
	public boolean isUseHardReference() {
		return useHardReference;
	}

	@Override
	public String toString() {
		return name + ": " + (value == null ? "NULL" : value.toString());
	}
}
