package com.AMS.jBEAM.javaParser.utils;

import com.AMS.jBEAM.javaParser.EvaluationMode;
import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.javaParser.Variable;
import com.AMS.jBEAM.javaParser.VariablePool;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.*;
import java.util.List;

public class ObjectInfoProvider
{
	private static TypeToken<?> getType(Object object, TypeToken<?> declaredType, EvaluationMode evaluationMode) {
		if (evaluationMode == EvaluationMode.DUCK_TYPING && object != null) {
			Class<?> runtimeClass = object.getClass();
			TypeToken<?> runtimeType = declaredType.getSubtype(runtimeClass);
			return declaredType.isPrimitive()
					? TypeToken.of(ReflectionUtils.getPrimitiveClass(runtimeType.getRawType()))
					: runtimeType;
		} else {
			return declaredType;
		}
	}

	static TypeToken<?> getType(ObjectInfo objectInfo, EvaluationMode evaluationMode) {
		return getType(objectInfo.getObject(), objectInfo.getDeclaredType(), evaluationMode);
	}

	static ObjectInfo getCastInfo(ObjectInfo objectInfo, TypeToken<?> targetType, EvaluationMode evaluationMode) throws ClassCastException {
		Object castedValue = evaluationMode == EvaluationMode.NONE
				? null
				: ReflectionUtils.convertTo(objectInfo.getObject(), targetType.getRawType(), true);
		return new ObjectInfo(castedValue, targetType);
	}

	private final EvaluationMode evaluationMode;

	public ObjectInfoProvider(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	private TypeToken<?> getType(Object object, TypeToken<?> declaredType) {
		return getType(object, declaredType, evaluationMode);
	}

	public TypeToken<?> getType(ObjectInfo objectInfo) {
		return getType(objectInfo, evaluationMode);
	}

	public ObjectInfo getFieldInfo(ObjectInfo contextInfo, FieldInfo fieldInfo) throws NullPointerException {
		final Object fieldValue;
		Object contextObject = contextInfo.getObject();
		if (evaluationMode == EvaluationMode.NONE) {
			fieldValue = null;
		} else {
			try {
				fieldValue = fieldInfo.get(contextObject);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Internal error: Unexpected IllegalAccessException: " + e.getMessage());
			}
		}
		ObjectInfo.ValueSetterIF valueSetter = getFieldValueSetter(contextObject, fieldInfo);
		return new ObjectInfo(fieldValue, fieldInfo.getType(), valueSetter);
	}

	private ObjectInfo.ValueSetterIF getFieldValueSetter(Object contextObject, FieldInfo fieldInfo) {
		if (fieldInfo.isFinal()) {
			return null;
		}
		return value -> {
			try {
				fieldInfo.set(contextObject, value);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Could not set field value", e);
			}
		};
	}

	public ObjectInfo getExecutableReturnInfo(ObjectInfo contextInfo, ExecutableInfo executableInfo, List<ObjectInfo> argumentInfos) throws NullPointerException {
		final Object methodReturnValue;
		if (evaluationMode == EvaluationMode.NONE) {
			methodReturnValue = null;
		} else {
			Object contextObject = executableInfo.isStatic() ? null : contextInfo.getObject();
			Object[] arguments = executableInfo.createArgumentArray(argumentInfos);
			try {
				methodReturnValue = executableInfo.invoke(contextObject, arguments);
			} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
				throw new IllegalStateException("Internal error: Unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			}
		}
		TypeToken<?> methodReturnType = getType(methodReturnValue, executableInfo.getReturnType());
		return new ObjectInfo(methodReturnValue, methodReturnType);
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
		TypeToken<?> arrayElementType = getType(arrayElementValue, getType(arrayInfo).getComponentType());
		return new ObjectInfo(arrayElementValue, arrayElementType, valueSetter);
	}

	public ObjectInfo getCastInfo(ObjectInfo objectInfo, TypeToken<?> targetType) throws ClassCastException {
		return getCastInfo(objectInfo, targetType, evaluationMode);
	}

	public ObjectInfo getVariableInfo(Variable variable, VariablePool variablePool) {
		Object value = variable.getValue();
		ObjectInfo.ValueSetterIF valueSetter = newValue -> variable.setValue(newValue);
		return new ObjectInfo(value, null, valueSetter);
	}
}
