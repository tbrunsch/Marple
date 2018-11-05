package com.AMS.jBEAM.javaParser;

import java.lang.reflect.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractJavaEntityParser
{
    final JavaParserPool 	parserPool;
    final ObjectInfo 		thisInfo;

    AbstractJavaEntityParser(JavaParserPool parserPool, ObjectInfo thisInfo) {
        this.parserPool = parserPool;
        this.thisInfo = thisInfo;
    }

    abstract ParseResultIF doParse(JavaTokenStream tokenStream, ObjectInfo currentContextInfo, Class<?> expectedResultClass);

    ParseResultIF parse(final JavaTokenStream tokenStream, ObjectInfo currentContextInfo, final Class<?> expectedResultClass) {
        return doParse(tokenStream.clone(), currentContextInfo, expectedResultClass);
    }

    private Class<?> getClass(Object object, Class<?> declaredClass) {
		return parserPool.getEvaluationMode() == EvaluationMode.DUCK_TYPING && object != null
				? object.getClass()
				: declaredClass;
	}

    Class<?> getClass(ObjectInfo objectInfo) {
    	return getClass(objectInfo.getObject(), objectInfo.getDeclaredClass());
	}

	ObjectInfo getFieldInfo(ObjectInfo contextInfo, Field field) throws NullPointerException {
    	final Object fieldValue;
		if (parserPool.getEvaluationMode() == EvaluationMode.NONE) {
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

	ObjectInfo getMethodReturnInfo(ObjectInfo contextInfo, Method method, ObjectInfo[] argumentInfos) throws NullPointerException {
		final Object methodReturnValue;
		if (parserPool.getEvaluationMode() == EvaluationMode.NONE) {
			methodReturnValue = null;
		} else {
			Object contextObject = (method.getModifiers() & Modifier.STATIC) != 0 ? null : contextInfo.getObject();
			Object[] arguments = new Object[argumentInfos.length];
			Class<?>[] argumentTypes = method.getParameterTypes();
			for (int i = 0; i < argumentTypes.length; i++) {
				Object argument = argumentInfos[i].getObject();
				arguments[i] = ReflectionUtils.convertTo(argument, argumentTypes[i]);
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

	ObjectInfo getArrayElementInfo(ObjectInfo arrayInfo, ObjectInfo indexInfo) throws NullPointerException {
    	final Object arrayElementValue;
    	if (parserPool.getEvaluationMode() == EvaluationMode.NONE) {
			arrayElementValue = null;
		} else {
    		Object arrayObject = arrayInfo.getObject();
			Object indexObject = indexInfo.getObject();
			int index = ReflectionUtils.convertTo(indexObject, int.class);
			arrayElementValue = Array.get(arrayObject, index);
		}
		Class<?> arrayElementClass = getClass(arrayElementValue, getClass(arrayInfo).getComponentType());
    	return new ObjectInfo(arrayElementValue, arrayElementClass);
	}

    CompletionSuggestions suggestFieldsAndMethods(ObjectInfo contextInfo, Class<?> expectedResultClass, final int insertionBegin, final int insertionEnd) {
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = new LinkedHashMap<>();

		Class<?> contextClass = getClass(contextInfo);

		List<Field> fields = parserPool.getInspectionDataProvider().getFields(contextClass, false);
		ratedSuggestions.putAll(ParseUtils.createRatedSuggestions(
				fields,
				field -> new CompletionSuggestionField(field, insertionBegin, insertionEnd),
				ParseUtils.rateFieldByClassFunc(expectedResultClass))
		);

		List<Method> methods = parserPool.getInspectionDataProvider().getMethods(contextClass, false);
		ratedSuggestions.putAll(ParseUtils.createRatedSuggestions(
				methods,
				method -> new CompletionSuggestionMethod(method, insertionBegin, insertionEnd),
				ParseUtils.rateMethodByClassFunc(expectedResultClass))
		);

		return new CompletionSuggestions(ratedSuggestions);
	}
}
