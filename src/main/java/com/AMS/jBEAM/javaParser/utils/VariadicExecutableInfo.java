package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.common.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.util.List;

import static com.AMS.jBEAM.javaParser.utils.ParseUtils.CLASS_MATCH_NONE;

public class VariadicExecutableInfo extends ExecutableInfo
{
	VariadicExecutableInfo(Executable executable) {
		super(executable);

		assert isVariadic() : "Cannot create VariadicExecutableInfo for non-variadic methods";
	}

	@Override
	public boolean isArgumentIndexValid(int argIndex) {
		return true;
	}

	@Override
	public Class<?> getExpectedArgumentType(int argIndex) {
		int lastIndex = getNumberOfArguments() - 1;
		return argIndex < lastIndex
				? executable.getParameterTypes()[argIndex]
				: executable.getParameterTypes()[lastIndex].getComponentType();
	}

	@Override
	public int rateArgumentMatch(List<Class<?>> argumentTypes) {
		if (argumentTypes.size() < getNumberOfArguments() - 1) {
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
		int lastArgIndex = getNumberOfArguments() - 1;
		if (argIndex == lastArgIndex && argumentType == null) {
			/*
			 * If the last argument in a variadic method is null, then the regular array type
			 * (the one returned in RegularExecutableInfo) is assumed and not its component type.
			 */
			return ParseUtils.CLASS_MATCH_NONE;
		}
		Class<?> expectedArgumentType = getExpectedArgumentType(argIndex);
		return ParseUtils.rateClassMatch(argumentType, expectedArgumentType);
	}

	@Override
	public Object[] createArgumentArray(List<ObjectInfo> argumentInfos) {
		int numArguments = getNumberOfArguments();
		int realNumArguments = argumentInfos.size();
		if (realNumArguments < numArguments - 1) {
			throw new IllegalArgumentException("Expected " + numArguments + " arguments, but number is " + realNumArguments);
		}

		Object[] arguments = new Object[numArguments];
		int variadicArgumentIndex = numArguments - 1;
		for (int i = 0; i < variadicArgumentIndex; i++) {
			Object argument = argumentInfos.get(i).getObject();
			arguments[i] = ReflectionUtils.convertTo(argument, executable.getParameterTypes()[i], false);
		}

		// variadic arguments exist
		int numVarArgs = realNumArguments - numArguments + 1;
		Class<?> varArgComponentType = executable.getParameterTypes()[variadicArgumentIndex].getComponentType();
		Object varArgArray = Array.newInstance(varArgComponentType, numVarArgs);
		for (int i = 0; i < numVarArgs; i++) {
			Object argument = argumentInfos.get(variadicArgumentIndex + i).getObject();
			Array.set(varArgArray, i, ReflectionUtils.convertTo(argument, varArgComponentType, false));
		}
		arguments[variadicArgumentIndex] = varArgArray;

		return arguments;
	}
}
