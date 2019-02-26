package com.AMS.jBEAM.javaParser.utils.wrappers;

import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.List;

public class RegularExecutableInfo extends ExecutableInfo
{
	RegularExecutableInfo(Executable executable, TypeInfo declaringType) {
		super(executable, declaringType);
	}

	@Override
	boolean doIsArgumentIndexValid(int argIndex) {
		return argIndex < getNumberOfArguments();
	}

	@Override
	Type doGetExpectedArgumentType(int argIndex) {
		if (argIndex >= getNumberOfArguments()) {
			throw new IndexOutOfBoundsException("Argument index " + argIndex + " is not in the range [0, " + getNumberOfArguments() + ")");
		}
		return executable.getGenericParameterTypes()[argIndex];
	}

	@Override
	int doRateArgumentMatch(List<TypeInfo> argumentTypes) {
		if (argumentTypes.size() != getNumberOfArguments()) {
			return ParseUtils.TYPE_MATCH_NONE;
		}
		int worstArgumentClassMatchRating = ParseUtils.TYPE_MATCH_FULL;
		for (int i = 0; i < argumentTypes.size(); i++) {
			int argumentClassMatchRating = rateArgumentTypeMatch(i, argumentTypes.get(i));
			worstArgumentClassMatchRating = Math.max(worstArgumentClassMatchRating, argumentClassMatchRating);
		}
		return worstArgumentClassMatchRating;
	}

	private int rateArgumentTypeMatch(int argIndex, TypeInfo argumentType) {
		TypeInfo expectedArgumentType = getExpectedArgumentType(argIndex);
		return ParseUtils.rateTypeMatch(argumentType, expectedArgumentType);
	}

	@Override
	Object[] doCreateArgumentArray(List<ObjectInfo> argumentInfos) {
		int numArguments = getNumberOfArguments();
		if (argumentInfos.size() != numArguments) {
			throw new IllegalArgumentException("Expected " + numArguments + " arguments, but number is " + argumentInfos.size());
		}

		Object[] arguments = new Object[numArguments];
		for (int i = 0; i < numArguments; i++) {
			Object argument = argumentInfos.get(i).getObject();
			arguments[i] = ReflectionUtils.convertTo(argument, getExpectedArgumentType(i).getRawType(), false);
		}
		return arguments;
	}
}
