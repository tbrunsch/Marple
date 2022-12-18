package dd.kms.marple.api.evaluator;

/**
 * Describes a value that can be referenced in an expression by its name.<br>
 * <br>
 * You can specify whether the value should be referenced with a hard or a weak reference.
 * If you decide for a weak reference, then the framework does not prolong the life time
 * of the variable's value to allow for garbage collection.
 */
public interface Variable
{
	static Variable create(String name, Class<?> type, Object value, boolean isFinal, boolean useHardReference) {
		return new dd.kms.marple.impl.evaluator.VariableImpl(name, type, value, isFinal, useHardReference);
	}

	String getName();
	Class<?> getType();
	Object getValue();
	boolean isFinal();
	boolean isUseHardReference();
}
