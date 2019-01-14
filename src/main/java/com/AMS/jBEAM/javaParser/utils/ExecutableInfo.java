package com.AMS.jBEAM.javaParser.utils;

import com.google.common.base.Joiner;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ExecutableInfo
{
	public static List<ExecutableInfo> getAvailableExecutableInfos(Executable executable) {
		return executable.isVarArgs()
				? Arrays.asList(new RegularExecutableInfo(executable), new VariadicExecutableInfo(executable))
				: Arrays.asList(new RegularExecutableInfo(executable));
	}

	protected final Executable	executable;

	ExecutableInfo(Executable executable) {
		this.executable = executable;
	}

	public abstract boolean isArgumentIndexValid(int argIndex);
	public abstract Class<?> getExpectedArgumentType(int argIndex);
	public abstract int rateArgumentMatch(List<Class<?>> argumentTypes);
	public abstract Object[] createArgumentArray(List<ObjectInfo> argumentInfos);

	public String getName() {
		return executable.getName();
	}

	public int getNumberOfArguments() {
		return executable.getParameterCount();
	}

	public boolean isVariadic() {
		return executable.isVarArgs();
	}

	public Class<?> getDeclaringClass() {
		return executable.getDeclaringClass();
	}

	public boolean isStatic() {
		return (executable.getModifiers() & Modifier.STATIC) != 0;
	}

	public Class<?> getReturnType() {
		return	executable instanceof Method	? ((Method) executable).getReturnType() :
		executable instanceof Constructor<?> 	? executable.getDeclaringClass()
												: null;
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
			String argumentTypeName = i < numArguments - 1 || !isVariadic()
										? executable.getParameterTypes()[i].getSimpleName()
										: executable.getParameterTypes()[i].getComponentType().getSimpleName() + "...";
			argumentTypeNames.add(argumentTypeName);
		}
		return Joiner.on(", ").join(argumentTypeNames);
	}
}
