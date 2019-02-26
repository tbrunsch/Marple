package com.AMS.jBEAM.javaParser.utils.wrappers;

import com.google.common.base.Joiner;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class ExecutableInfo
{
	public static List<ExecutableInfo> getAvailableExecutableInfos(Executable executable, TypeInfo declaringType) {
		return executable.isVarArgs()
				? Arrays.asList(new RegularExecutableInfo(executable, declaringType), new VariadicExecutableInfo(executable, declaringType))
				: Arrays.asList(new RegularExecutableInfo(executable, declaringType));
	}

	protected final Executable	executable;
	private final TypeInfo		declaringType;

	ExecutableInfo(Executable executable, TypeInfo declaringType) {
		this.executable = executable;
		this.declaringType = declaringType;
	}

	abstract boolean doIsArgumentIndexValid(int argIndex);
	abstract Type doGetExpectedArgumentType(int argIndex);
	abstract int doRateArgumentMatch(List<TypeInfo> argumentTypes);
	abstract Object[] doCreateArgumentArray(List<ObjectInfo> argumentInfos);

	public String getName() {
		return executable.getName();
	}

	public int getNumberOfArguments() {
		return executable.getParameterCount();
	}

	public boolean isVariadic() {
		return executable.isVarArgs();
	}

	public TypeInfo getDeclaringType() {
		return declaringType;
	}

	public boolean isStatic() {
		return Modifier.isStatic(executable.getModifiers());
	}

	public TypeInfo getReturnType() {
		return	executable instanceof Method	? declaringType.resolveType(((Method) executable).getGenericReturnType()) :
		executable instanceof Constructor<?> 	? declaringType
												: TypeInfo.NONE;
	}

	public final boolean isArgumentIndexValid(int argIndex) {
		return doIsArgumentIndexValid(argIndex);
	}

	public final TypeInfo getExpectedArgumentType(int argIndex) {
		return declaringType.resolveType(doGetExpectedArgumentType(argIndex));
	}

	public final int rateArgumentMatch(List<TypeInfo> argumentTypes) {
		return doRateArgumentMatch(argumentTypes);
	}

	public final Object[] createArgumentArray(List<ObjectInfo> argumentInfos) {
		return doCreateArgumentArray(argumentInfos);
	}

	public Object invoke(Object instance, Object[] arguments) throws InvocationTargetException, IllegalAccessException, InstantiationException {
		executable.setAccessible(true);
		return	executable instanceof Method			? ((Method) executable).invoke(instance, arguments) :
				executable instanceof Constructor<?>	? ((Constructor<?>) executable).newInstance(arguments)
														: null;
	}

	public String formatArguments() {
		int numArguments = getNumberOfArguments();
		List<String> argumentTypeNames = new ArrayList<>(numArguments);
		for (int i = 0; i < numArguments; i++) {
			TypeInfo argumentType = declaringType.resolveType(executable.getGenericParameterTypes()[i]);
			String argumentTypeName = i < numArguments - 1 || !isVariadic()
										? argumentType.toString()
										: argumentType.getComponentType().toString() + "...";
			argumentTypeNames.add(argumentTypeName);
		}
		return Joiner.on(", ").join(argumentTypeNames);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ExecutableInfo that = (ExecutableInfo) o;
		return Objects.equals(executable, that.executable) &&
				Objects.equals(declaringType, that.declaringType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(executable, declaringType);
	}

	@Override
	public String toString() {
		return getName()
				+ "("
				+ formatArguments()
				+ ")";
	}
}
