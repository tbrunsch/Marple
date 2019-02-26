package com.AMS.jBEAM.javaParser.utils.dataProviders;

import com.AMS.jBEAM.common.ReflectionUtils;
import com.AMS.jBEAM.javaParser.settings.EvaluationMode;
import com.AMS.jBEAM.javaParser.settings.Variable;
import com.AMS.jBEAM.javaParser.utils.wrappers.ExecutableInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.FieldInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.ObjectInfo;
import com.AMS.jBEAM.javaParser.utils.wrappers.TypeInfo;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ObjectInfoProvider
{
	private final EvaluationMode evaluationMode;

	public ObjectInfoProvider(EvaluationMode evaluationMode) {
		this.evaluationMode = evaluationMode;
	}

	public TypeInfo getType(Object object, TypeInfo declaredType) {
		if (object == null) {
			return declaredType == TypeInfo.UNKNOWN ? TypeInfo.NONE : declaredType;
		}

		Class<?> runtimeClass = object.getClass();
		if (declaredType == TypeInfo.UNKNOWN) {
			return TypeInfo.of(runtimeClass);
		}

		if (evaluationMode == EvaluationMode.DYNAMICALLY_TYPED) {
			if (declaredType.isPrimitive()) {
				return declaredType;
			}
			TypeInfo runtimeType = declaredType.getSubtype(runtimeClass);
			return declaredType.isPrimitive()
					? TypeInfo.of(ReflectionUtils.getPrimitiveClass(runtimeType.getRawType()))
					: runtimeType;
		} else {
			return declaredType;
		}
	}

	public TypeInfo getType(ObjectInfo objectInfo) {
		return getType(objectInfo.getObject(), objectInfo.getDeclaredType());
	}

	public ObjectInfo getFieldValueInfo(Object contextObject, FieldInfo fieldInfo) {
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

	public ObjectInfo getExecutableReturnInfo(Object contextObject, ExecutableInfo executableInfo, List<ObjectInfo> argumentInfos) throws InvocationTargetException, InstantiationException {
		final Object methodReturnValue;
		if (evaluationMode == EvaluationMode.NONE) {
			methodReturnValue = ObjectInfo.INDETERMINATE;
		} else {
			Object[] arguments = executableInfo.createArgumentArray(argumentInfos);
			try {
				methodReturnValue = executableInfo.invoke(contextObject, arguments);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Internal error: Unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
			}
		}
		TypeInfo methodReturnType = getType(methodReturnValue, executableInfo.getReturnType());
		return new ObjectInfo(methodReturnValue, methodReturnType);
	}

	public ObjectInfo getArrayElementInfo(ObjectInfo arrayInfo, ObjectInfo indexInfo) {
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
		TypeInfo arrayElementType = getType(arrayElementValue, getType(arrayInfo).getComponentType());
		return new ObjectInfo(arrayElementValue, arrayElementType, valueSetter);
	}

	public ObjectInfo getArrayInfo(TypeInfo componentType, ObjectInfo sizeInfo) {
		final int size;
		if (evaluationMode == EvaluationMode.NONE) {
			size = 0;
		} else {
			Object sizeObject = sizeInfo.getObject();
			size = ReflectionUtils.convertTo(sizeObject, int.class, false);
		}
		return getArrayInfo(componentType, size);
	}

	public ObjectInfo getArrayInfo(TypeInfo componentType, List<ObjectInfo> elementInfos) {
		int size = elementInfos.size();
		ObjectInfo arrayInfo = getArrayInfo(componentType, size);
		if (evaluationMode != EvaluationMode.NONE) {
			Class<?> componentClass = componentType.getRawType();
			Object arrayObject = arrayInfo.getObject();
			for (int i = 0; i < size; i++) {
				Object element = elementInfos.get(i).getObject();
				Array.set(arrayObject, i, ReflectionUtils.convertTo(element, componentClass, false));
			}
		}
		return arrayInfo;
	}

	private ObjectInfo getArrayInfo(TypeInfo componentType, int size) {
		Class<?> componentClass = componentType.getRawType();
		Object array = Array.newInstance(componentClass, size);
		Class<?> arrayClass = array.getClass();
		TypeInfo arrayType = TypeInfo.of(arrayClass);
		Object arrayObject = evaluationMode == EvaluationMode.NONE ? ObjectInfo.INDETERMINATE : array;
		return new ObjectInfo(arrayObject, arrayType);
	}

	public ObjectInfo getCastInfo(ObjectInfo objectInfo, TypeInfo targetType) throws ClassCastException {
		Object castedValue = evaluationMode == EvaluationMode.NONE
								? ObjectInfo.INDETERMINATE
								: ReflectionUtils.convertTo(objectInfo.getObject(), targetType.getRawType(), true);
		return new ObjectInfo(castedValue, targetType);
	}

	public ObjectInfo getVariableInfo(Variable variable) {
		Object value = variable.getValue();
		ObjectInfo.ValueSetterIF valueSetter = newValue -> variable.setValue(newValue);
		return new ObjectInfo(value, TypeInfo.UNKNOWN, valueSetter);
	}
}
