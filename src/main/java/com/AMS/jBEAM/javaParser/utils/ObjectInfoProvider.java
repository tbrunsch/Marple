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
	static TypeToken<?> getType(Object object, TypeToken<?> declaredType, EvaluationMode evaluationMode) {
		if (object == null) {
			return declaredType;
		}

		Class<?> runtimeClass = object.getClass();
		if (declaredType == null) {
			return TypeToken.of(runtimeClass);
		}

		if (evaluationMode == EvaluationMode.DUCK_TYPING) {
			if (declaredType.isPrimitive()) {
				return declaredType;
			}
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
				? ObjectInfo.INDETERMINATE
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
		Object contextObject = contextInfo.getObject();
		Object fieldValue = ObjectInfo.INDETERMINATE;
		if (evaluationMode != EvaluationMode.NONE) {
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
			methodReturnValue = ObjectInfo.INDETERMINATE;
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
			arrayElementValue = ObjectInfo.INDETERMINATE;
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

	public ObjectInfo getArrayInfo(ObjectInfo classInfo, ObjectInfo sizeInfo) {
		final int size;
		if (evaluationMode == EvaluationMode.NONE) {
			size = 0;
		} else {
			Object sizeObject = sizeInfo.getObject();
			size = ReflectionUtils.convertTo(sizeObject, int.class, false);
		}
		return getArrayInfo(classInfo, size);
	}

	public ObjectInfo getArrayInfo(ObjectInfo classInfo, List<ObjectInfo> elementInfos) {
		int size = elementInfos.size();
		ObjectInfo arrayInfo = getArrayInfo(classInfo, size);
		if (evaluationMode != EvaluationMode.NONE) {
			Class<?> componentClass = classInfo.getDeclaredType().getRawType();
			Object arrayObject = arrayInfo.getObject();
			for (int i = 0; i < size; i++) {
				Object element = elementInfos.get(i).getObject();
				Array.set(arrayObject, i, ReflectionUtils.convertTo(element, componentClass, false));
			}
		}
		return arrayInfo;
	}

	private ObjectInfo getArrayInfo(ObjectInfo classInfo, int size) {
		Class<?> componentClass = classInfo.getDeclaredType().getRawType();
		Object array = Array.newInstance(componentClass, size);
		Class<?> arrayClass = array.getClass();
		TypeToken<?> arrayType = TypeToken.of(arrayClass);
		Object arrayObject = evaluationMode == EvaluationMode.NONE ? ObjectInfo.INDETERMINATE : array;
		return new ObjectInfo(arrayObject, arrayType);
	}

	public ObjectInfo getCastInfo(ObjectInfo objectInfo, TypeToken<?> targetType) throws ClassCastException {
		return getCastInfo(objectInfo, targetType, evaluationMode);
	}

	public ObjectInfo getVariableInfo(Variable variable) {
		Object value = variable.getValue();
		ObjectInfo.ValueSetterIF valueSetter = newValue -> variable.setValue(newValue);
		return new ObjectInfo(value, null, valueSetter);
	}
}
