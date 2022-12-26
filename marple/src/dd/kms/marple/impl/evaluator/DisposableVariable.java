package dd.kms.marple.impl.evaluator;

import java.lang.ref.WeakReference;

class DisposableVariable extends AbstractVariable
{
	/**
	 * Either {@link #weakValueReference} or {@link #hardValueReference} are used,
	 * depending on whether a hard references should be used for this variable or not.
	 */
	private final WeakReference<Object> weakValueReference;	// only used
	private final Object				hardValueReference;	// only set if user wants to save variables from being garbage collected
	private final boolean				valueIsNull;

	public DisposableVariable(String name, Class<?> type, Object value, boolean isFinal, boolean useHardReference) {
		super(name, type, isFinal);
		valueIsNull = value == null;
		if (useHardReference) {
			hardValueReference = value;
			weakValueReference = null;
		} else {
			hardValueReference = null;
			weakValueReference = new WeakReference<>(value);
		}
	}

	@Override
	public Object getValue() {
		return isUseHardReference() ? hardValueReference : weakValueReference.get();
	}

	@Override
	public boolean isUseHardReference() {
		return weakValueReference == null;
	}

	boolean isGarbageCollected() {
		return !valueIsNull && getValue() == null;
	}

	@Override
	public String toString() {
		Object value = getValue();
		return getName() + ": " +
				(isGarbageCollected() ? "garbage collected" : String.valueOf(value));
	}
}
