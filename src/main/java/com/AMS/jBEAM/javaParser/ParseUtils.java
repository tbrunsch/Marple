package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

class ParseUtils
{
    /*
     * String Comparison
     */
    private static final int STRING_MATCH_FULL 							= 0;
    private static final int STRING_MATCH_FULL_IGNORE_CASE 				= 1;
    private static final int STRING_MATCH_PREFIX                        = 2;
    private static final int STRING_MATCH_PREFIX_IGNORE_CASE            = 3;
    private static final int STRING_MATCH_INVERSE_PREFIX                = 4;
    private static final int STRING_MATCH_INVERSE_PREFIX_IGNORE_CASE    = 5;
    private static final int STRING_MATCH_MIN_VALUE_OTHER               = 6;

    private static int rateStringMatch(String actual, String expected) {
        if (actual.equals(expected)) {
            return STRING_MATCH_FULL;
        } else {
            String actualLowerCase = actual.toLowerCase();
            String expectedLowerCase = expected.toLowerCase();
            if (actualLowerCase.equals(expectedLowerCase)) {
                return STRING_MATCH_FULL_IGNORE_CASE;
            } else if (actual.startsWith(expected)) {
                return STRING_MATCH_PREFIX;
            } else if (actualLowerCase.startsWith(expectedLowerCase)) {
                return STRING_MATCH_PREFIX_IGNORE_CASE;
            } else if (expected.startsWith(actual)) {
                return STRING_MATCH_INVERSE_PREFIX;
            } else if (expectedLowerCase.startsWith(actualLowerCase)) {
                return STRING_MATCH_INVERSE_PREFIX_IGNORE_CASE;
            } else {
                // TODO: Differentiate between Strings
                return STRING_MATCH_MIN_VALUE_OTHER;
            }
        }
    }

    private static final int CLASS_MATCH_FULL					= 0;
    private static final int CLASS_MATCH_PRIMITIVE_BOXED		= 1;
    private static final int CLASS_MATCH_INHERITANCE			= 2;
    private static final int CLASS_MATCH_BOXED_AND_INHERITANCE  = 3;
    private static final int CLASS_MATCH_PRIMITIVE_CONVERSION	= 4;
    private static final int CLASS_MATCH_NONE                   = 5;

    private static int rateClassMatch(Class<?> actual, Class<?> expected) {
    	if (expected == null) {
    		// No expectations
			return CLASS_MATCH_FULL;
		}

        if (actual == expected) {
            return CLASS_MATCH_FULL;
        }
        if (actual.isPrimitive()) {
            Class<?> boxedClass = ReflectionUtils.getBoxedClass(actual);
            if (boxedClass == expected) {
                // e.g. actual == int.class, expected = Integer.class
                return CLASS_MATCH_PRIMITIVE_BOXED;
            }
            if (expected.isAssignableFrom(boxedClass)) {
                // e.g. actual == int.class, expected == Number.class
                return CLASS_MATCH_BOXED_AND_INHERITANCE;
            }
        } else {
            Class<?> primitiveClass = ReflectionUtils.getPrimitiveClass(actual);
            if (primitiveClass == expected) {
                // e.g. actual == Integer.class, expected == int.class
                return CLASS_MATCH_PRIMITIVE_BOXED;
            }
            if (expected.isAssignableFrom(actual)) {
                // e.g. actual == Integer.class, expected == Number.class
                return CLASS_MATCH_INHERITANCE;
            }
        }
        if (ReflectionUtils.isPrimitiveConvertibleTo(actual, expected)) {
            // e.g. actual == int.class or Integer.class, expected == double.class or Double.class
            return CLASS_MATCH_PRIMITIVE_CONVERSION;
        }
        return CLASS_MATCH_NONE;
    }

    public static boolean isConvertibleTo(Class<?> source, Class<?> target) {
        return rateClassMatch(source, target) != CLASS_MATCH_NONE;
    }

    /*
     * Fields
     */
    static final Function<Field, String>    FIELD_DISPLAY_FUNC          = ParseUtils::getFieldDisplayText;

    static ToIntFunction<Field> rateFieldByNameFunc(final String expectedFieldName) {
        return field -> rateFieldByName(field, expectedFieldName);
    }

    private static int rateFieldByName(Field field, String expectedFieldName) {
        return rateStringMatch(field.getName(), expectedFieldName);
    }

    static ToIntFunction<Field> rateFieldByClassFunc(final Class<?> expectedClass) {
        return field -> rateFieldByClass(field, expectedClass);
    }

    private static int rateFieldByClass(Field field, Class<?> expectedClass) {
        return rateClassMatch(field.getType(), expectedClass);
    }

    static ToIntFunction<Field> rateFieldByNameAndClassFunc(final String fieldName, final Class<?> expectedClass) {
        return field -> (CLASS_MATCH_NONE +1)*rateFieldByName(field, fieldName) + rateFieldByClass(field, expectedClass);
    }

    static Function<Field, TextInsertionInfo> fieldTextInsertionInfoFunction(final int insertionBegin, final int insertionEnd) {
        return field -> new TextInsertionInfo(new IntRange(insertionBegin, insertionEnd),
                                                insertionBegin + field.getName().length(),
                                                field.getName());
    }

    private static String getFieldDisplayText(Field field) {
        return field.getName() + " (" + field.getDeclaringClass().getSimpleName() + ")";
    }

    /*
     * Methods
     */
    static final Function<Method, String>   METHOD_DISPLAY_FUNC = ParseUtils::getMethodDisplayText;

    static ToIntFunction<Method> rateMethodByNameFunc(final String expectedMethodName) {
        return method -> rateMethodByName(method, expectedMethodName);
    }

    private static int rateMethodByName(Method method, String expectedMethodName) {
        return rateStringMatch(method.getName(), expectedMethodName);
    }

    static ToIntFunction<Method> rateMethodByClassFunc(final Class<?> expectedClass) {
        return method -> rateMethodByClass(method, expectedClass);
    }

    private static int rateMethodByClass(Method method, Class<?> expectedClass) {
        return rateClassMatch(method.getReturnType(), expectedClass);
    }

    static ToIntFunction<Method> rateMethodByNameAndClassFunc(final String methodName, final Class<?> expectedClass) {
        return method -> (CLASS_MATCH_NONE +1)*rateMethodByName(method, methodName) + rateMethodByClass(method, expectedClass);
    }

    static Function<Method, TextInsertionInfo> methodTextInsertionInfoFunction(final int insertionBegin, final int insertionEnd) {
        return method -> new TextInsertionInfo(new IntRange(insertionBegin, insertionEnd),
                                                insertionBegin + method.getName().length() + 1,
                                                method.getName()
                                                    + "("
                                                    + Arrays.stream(method.getParameters()).map(Parameter::getName).collect(Collectors.joining(", "))
                                                    + ")");
    }

    private static String getMethodDisplayText(Method method) {
        return method.getName() + " (" + method.getDeclaringClass().getSimpleName() + ")";
    }

    /*
     * Completion Suggestions
     */
    static <T> List<CompletionSuggestion> createSuggestions(List<T> objects, Function<T, TextInsertionInfo> textInsertionInfoFunc, Function<T, String> displayTextFunc, ToIntFunction<T> objectRatingFunc) {
        return objects.stream()
                .map(object -> new CompletionSuggestion(
                    textInsertionInfoFunc.apply(object),
                    displayTextFunc.apply(object),
                    objectRatingFunc.applyAsInt(object)))
                .collect(Collectors.toList());
    }
}
