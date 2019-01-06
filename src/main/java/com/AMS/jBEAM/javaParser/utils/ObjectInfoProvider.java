package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.EvaluationMode;
import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.javaParser.Variable;
import com.AMS.jBEAM.javaParser.VariablePoolIF;

import java.lang.reflect.*;
import java.util.List;

public class ObjectInfoProvider
{
	private static Class<?> getClass(Object object, Class<?> declaredClass, EvaluationMode evaluationMode) {
		if (evaluationMode == EvaluationMode.DUCK_TYPING && object != null) {
			Class<?> clazz = object.getClass();
			return declaredClass.isPrimitive()
					? ReflectionUtils.getPrimitiveClass(clazz)
					: clazz;
		} else {
			return declaredClass;
		}
	}

	static Class<?> getClass(ObjectInfo objectInfo, EvaluationMode evaluationMode) {
		return getClass(objectInfo.getObject(), objectInfo.getDeclaredClass(), evaluationMode);
	}

	static ObjectInfo getCastInfo(ObjectInfo objectInfo, Class<?> targetClass, EvaluationMode evaluationMode) throws ClassCastException {
		Object castedValue = evaluationMode == EvaluationMode.NONE
				? null
				: ReflectionUtils.convertTo(objectInfo.getObject(), targetClass, true);
		return new ObjectInfo(castedValue, targetClass);
	}

	private final EvaluationMode evaluationMode;

	public ObjectInfoProvider(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	private Class<?> getClass(Object object, Class<?> declaredClass) {
		return getClass(object, declaredClass, evaluationMode);
	}

	public Class<?> getClass(ObjectInfo objectInfo) {
		return getClass(objectInfo, evaluationMode);
	}

	public ObjectInfo getFieldInfo(ObjectInfo contextInfo, Field field) throws NullPointerException {
		final Object fieldValue;
		int modifiers = field.getModifiers();
		Object contextObject = (modifiers & Modifier.STATIC) != 0 ? null : contextInfo.getObject();
		if (evaluationMode == EvaluationMode.NONE) {
			fieldValue = null;
		} else {
			try {
				field.setAccessible(true);
				fieldValue = field.get(contextObject);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Internal error: Unexpected IllegalAccessException: " + e.getMessage());
			}
		}
		ObjectInfo.ValueSetterIF valueSetter = getFieldValueSetter(contextObject, field);
		Class<?> fieldClass = getClass(fieldValue, field.getType());
		return new ObjectInfo(fieldValue, fieldClass, valueSetter);
	}

	private ObjectInfo.ValueSetterIF getFieldValueSetter(Object contextObject, Field field) {
		int modifiers = field.getModifiers();
		if ((modifiers & Modifier.FINAL) != 0) {
			return null;
		}
		return value -> {
			try {
				field.set(contextObject, value);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Could not set field value", e);
			}
		};
	}

	public ObjectInfo getMethodReturnInfo(ObjectInfo contextInfo, Executable method, List<ObjectInfo> argumentInfos) throws NullPointerException {
		final Object methodReturnValue;
		if (evaluationMode == EvaluationMode.NONE) {
			methodReturnValue = null;
		} else {
			Object contextObject = (method.getModifiers() & Modifier.STATIC) != 0 ? null : contextInfo.getObject();
			Object[] arguments = new Object[argumentInfos.size()];
			Class<?>[] argumentTypes = method.getParameterTypes();
			for (int i = 0; i < argumentTypes.length; i++) {
				Object argument = argumentInfos.get(i).getObject();
				arguments[i] = ReflectionUtils.convertTo(argument, argumentTypes[i], false);
			}
			try {
				method.setAccessible(true);
				if (method instanceof Method) {
					methodReturnValue = ((Method) method).invoke(contextObject, arguments);
				} else if (method instanceof Constructor<?>) {
					methodReturnValue = ((Constructor<?>) method).newInstance(arguments);
				} else {
					throw new IllegalArgumentException("Method '" + method.getName() + "' is neither a method nor a constructor");
				}
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Internal error: Unexpected IllegalAccessException: " + e.getMessage(), e);
			} catch (InvocationTargetException e) {
				throw new IllegalStateException("Internal error: Unexpected InvocationTargetException: " + e.getMessage(), e);
			} catch (InstantiationException e) {
				throw new IllegalStateException("Internal error: Unexpected InstantiationException: " + e.getMessage(), e);
			}
		}
		Class<?> methodReturnClass;
		if (method instanceof Method) {
			methodReturnClass = getClass(methodReturnValue, ((Method) method).getReturnType());
		} else if (method instanceof Constructor<?>) {
			methodReturnClass = method.getDeclaringClass();
		} else {
			throw new IllegalArgumentException("Method '" + method.getName() + "' is neither a method nor a constructor");
		}
		return new ObjectInfo(methodReturnValue, methodReturnClass);
	}

	public ObjectInfo getArrayElementInfo(ObjectInfo arrayInfo, ObjectInfo indexInfo) throws NullPointerException {
		final Object arrayElementValue;
		final ObjectInfo.ValueSetterIF valueSetter;
		if (evaluationMode == EvaluationMode.NONE) {
			arrayElementValue = null;
			valueSetter = null;
		} else {
			Object arrayObject = arrayInfo.getObject();
			Object indexObject = indexInfo.getObject();
			int index = ReflectionUtils.convertTo(indexObject, int.class, false);
			arrayElementValue = Array.get(arrayObject, index);
			valueSetter = value -> Array.set(arrayObject, index, value);
		}
		Class<?> arrayElementClass = getClass(arrayElementValue, getClass(arrayInfo).getComponentType());
		return new ObjectInfo(arrayElementValue, arrayElementClass, valueSetter);
	}

	public ObjectInfo getCastInfo(ObjectInfo objectInfo, Class<?> targetClass) throws ClassCastException {
		return getCastInfo(objectInfo, targetClass, evaluationMode);
	}

	public ObjectInfo getVariableInfo(Variable variable, VariablePoolIF variablePool) {
		final String name = variable.getName();
		final boolean useHardReference = variable.isUseHardReferenceInPool();
		Object value = variable.getValue();
		Class<?> clazz = value == null ? null : value.getClass();
		ObjectInfo.ValueSetterIF valueSetter = object -> variablePool.addVariable(new Variable(name, object, useHardReference));
		return new ObjectInfo(value, clazz, valueSetter);
	}
}
