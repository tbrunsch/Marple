package com.AMS.jBEAM.javaParser;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Test
{
	public static <T> T[] toArray(T... elements) { return elements; }

	public static void main(String[] args) {
		Array.newInstance(int.class, -1);

		TypeToken<?> argType = TypeToken.of(args.getClass());
		Method toArrayMethod = Arrays.stream(Test.class.getMethods()).filter(m -> m.getName().equals("toArray")).findFirst().orElse(null);
		TypeToken<?> resolvedArgType = TypeToken.of(toArrayMethod.getGenericParameterTypes()[0]).resolveType(args.getClass());

		//TypeToken<?> resolvedReturnType = argType.resolveType((Type) returnType);
		System.out.println(resolvedArgType);
	}
}
