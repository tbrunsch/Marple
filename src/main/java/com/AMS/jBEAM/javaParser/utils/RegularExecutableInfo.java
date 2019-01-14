package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.common.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.util.List;

import static com.AMS.jBEAM.javaParser.utils.ParseUtils.CLASS_MATCH_NONE;

public class RegularExecutableInfo extends ExecutableInfo
{
	RegularExecutableInfo(Executable executable) {
		super(executable);
	}

	@Override
	public boolean isArgumentIndexValid(int argIndex) {
		return argIndex < getNumberOfArguments();
	}

	@Override
	public Class<?> getExpectedArgumentType(int argIndex) {
		if (argIndex >= getNumberOfArguments()) {
			throw new IndexOutOfBoundsException("Argument index " + argIndex + " is not in the range [0, " + getNumberOfArguments() + ")");
		}
		return executable.getParameterTypes()[argIndex];
	}

	@Override
	public int rateArgumentMatch(List<Class<?>> argumentTypes) {
		if (argumentTypes.size() != getNumberOfArguments()) {
			return ParseUtils.CLASS_MATCH_NONE;
		}
		int worstArgumentClassMatchRating = ParseUtils.CLASS_MATCH_FULL;
		for (int i = 0; i < argumentTypes.size(); i++) {
			int argumentClassMatchRating = rateArgumentTypeMatch(i, argumentTypes.get(i));
			worstArgumentClassMatchRating = Math.max(worstArgumentClassMatchRating, argumentClassMatchRating);
		}
		return worstArgumentClassMatchRating;
	}

	private int rateArgumentTypeMatch(int argIndex, Class<?> argumentType) {
		Class<?> expectedArgumentType = getExpectedArgumentType(argIndex);
		return ParseUtils.rateClassMatch(argumentType, expectedArgumentType);
	}

	@Override
	public Object[] createArgumentArray(List<ObjectInfo> argumentInfos) {
		int numArguments = getNumberOfArguments();
		if (argumentInfos.size() != numArguments) {
			throw new IllegalArgumentException("Expected " + numArguments + " arguments, but number is " + argumentInfos.size());
		}

		Object[] arguments = new Object[numArguments];
		for (int i = 0; i < numArguments; i++) {
			Object argument = argumentInfos.get(i).getObject();
			arguments[i] = ReflectionUtils.convertTo(argument, executable.getParameterTypes()[i], false);
		}
		return arguments;
	}
}
