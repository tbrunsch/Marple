package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.EvaluationMode;
import com.AMS.jBEAM.common.ReflectionUtils;

import java.lang.reflect.*;
import java.util.List;

public class ObjectInfoProvider
{
	private final EvaluationMode evaluationMode;

	public ObjectInfoProvider(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	private Class<?> getClass(Object object, Class<?> declaredClass) {
		if (evaluationMode == EvaluationMode.DUCK_TYPING && object != null) {
			Class<?> clazz = object.getClass();
			return declaredClass.isPrimitive()
					? ReflectionUtils.getPrimitiveClass(clazz)
					: clazz;
		} else {
			return declaredClass;
		}
	}

	public Class<?> getClass(ObjectInfo objectInfo) {
		return getClass(objectInfo.getObject(), objectInfo.getDeclaredClass());
	}

	public ObjectInfo getFieldInfo(ObjectInfo contextInfo, Field field) throws NullPointerException {
		final Object fieldValue;
		if (evaluationMode == EvaluationMode.NONE) {
			fieldValue = null;
		} else {
			Object contextObject = (field.getModifiers() & Modifier.STATIC) != 0 ? null : contextInfo.getObject();
			try {
				field.setAccessible(true);
				fieldValue = field.get(contextObject);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Internal error: Unexpected IllegalAccessException: " + e.getMessage());
			}
		}
		Class<?> fieldClass = getClass(fieldValue, field.getType());
		return new ObjectInfo(fieldValue, fieldClass);
	}

	public ObjectInfo getMethodReturnInfo(ObjectInfo contextInfo, Method method, List<ObjectInfo> argumentInfos) throws NullPointerException {
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
				methodReturnValue = method.invoke(contextObject, arguments);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Internal error: Unexpected IllegalAccessException: " + e.getMessage());
			} catch (InvocationTargetException e) {
				throw new IllegalStateException("Internal error: Unexpected InvocationTargetException: " + e.getMessage());
			}
		}
		Class<?> methodReturnClass = getClass(methodReturnValue, method.getReturnType());
		return new ObjectInfo(methodReturnValue, methodReturnClass);
	}

	public ObjectInfo getArrayElementInfo(ObjectInfo arrayInfo, ObjectInfo indexInfo) throws NullPointerException {
		final Object arrayElementValue;
		if (evaluationMode == EvaluationMode.NONE) {
			arrayElementValue = null;
		} else {
			Object arrayObject = arrayInfo.getObject();
			Object indexObject = indexInfo.getObject();
			int index = ReflectionUtils.convertTo(indexObject, int.class, false);
			arrayElementValue = Array.get(arrayObject, index);
		}
		Class<?> arrayElementClass = getClass(arrayElementValue, getClass(arrayInfo).getComponentType());
		return new ObjectInfo(arrayElementValue, arrayElementClass);
	}

	public ObjectInfo getCastInfo(ObjectInfo objectInfo, Class<?> targetClass) throws ClassCastException {
		Object castedValue = evaluationMode == EvaluationMode.NONE
								? null
								: ReflectionUtils.convertTo(objectInfo.getObject(), targetClass, true);
		return new ObjectInfo(castedValue, targetClass);
	}
}
