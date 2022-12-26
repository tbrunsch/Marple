package dd.kms.marple.impl.evaluator;

public class DefaultVariable extends AbstractVariable
{
	private final Object	value;
	private final boolean	useHardReference;

	public DefaultVariable(String name, Class<?> type, Object value, boolean isFinal, boolean useHardReference) {
		super(name, type, isFinal);
		this.value = value;
		this.useHardReference = useHardReference;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public boolean isUseHardReference() {
		return useHardReference;
	}

	@Override
	public String toString() {
		return getName() + ": " + (value == null ? "NULL" : value.toString());
	}
}
