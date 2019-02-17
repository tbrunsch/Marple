package com.AMS.jBEAM.javaParser.utils.wrappers;

import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.javaParser.utils.ParseUtils;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.List;

public class VariadicExecutableInfo extends ExecutableInfo
{
	VariadicExecutableInfo(Executable executable, TypeToken<?> declaringType) {
		super(executable, declaringType);

		assert isVariadic() : "Cannot create VariadicExecutableInfo for non-variadic methods";
	}

	@Override
	boolean doIsArgumentIndexValid(int argIndex) {
		return true;
	}

	@Override
	Type doGetExpectedArgumentType(int argIndex) {
		int lastIndex = getNumberOfArguments() - 1;
		return argIndex < lastIndex
				? executable.getGenericParameterTypes()[argIndex]
				: TypeToken.of(executable.getGenericParameterTypes()[lastIndex]).getComponentType().getType();
	}

	@Override
	int doRateArgumentMatch(List<TypeToken<?>> argumentTypes) {
		if (argumentTypes.size() < getNumberOfArguments() - 1) {
			return ParseUtils.TYPE_MATCH_NONE;
		}
		int worstArgumentClassMatchRating = ParseUtils.TYPE_MATCH_FULL;
		for (int i = 0; i < argumentTypes.size(); i++) {
			int argumentClassMatchRating = rateArgumentTypeMatch(i, argumentTypes.get(i));
			worstArgumentClassMatchRating = Math.max(worstArgumentClassMatchRating, argumentClassMatchRating);
		}
		return worstArgumentClassMatchRating;
	}

	private int rateArgumentTypeMatch(int argIndex, TypeToken<?> argumentType) {
		int lastArgIndex = getNumberOfArguments() - 1;
		if (argIndex == lastArgIndex && argumentType == null) {
			/*
			 * If the last argument in a variadic method is null, then the regular array type
			 * (the one returned in RegularExecutableInfo) is assumed and not its component type.
			 */
			return ParseUtils.TYPE_MATCH_NONE;
		}
		TypeToken<?> expectedArgumentType = getExpectedArgumentType(argIndex);
		return ParseUtils.rateTypeMatch(argumentType, expectedArgumentType);
	}

	@Override
	Object[] doCreateArgumentArray(List<ObjectInfo> argumentInfos) {
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
		Class<?> varArgComponentClass = executable.getParameterTypes()[variadicArgumentIndex].getComponentType();
		Object varArgArray = Array.newInstance(varArgComponentClass, numVarArgs);
		for (int i = 0; i < numVarArgs; i++) {
			Object argument = argumentInfos.get(variadicArgumentIndex + i).getObject();
			Array.set(varArgArray, i, ReflectionUtils.convertTo(argument, varArgComponentClass, false));
		}
		arguments[variadicArgumentIndex] = varArgArray;

		return arguments;
	}
}
