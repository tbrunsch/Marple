package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    		// no expectations
			return CLASS_MATCH_FULL;
		}

		if (actual == null) {
			// null object is convertible to any non-primitive class
			return expected.isPrimitive() ? CLASS_MATCH_NONE : CLASS_MATCH_FULL;
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

    static String getFieldDisplayText(Field field) {
        return field.getName() + " (" + field.getDeclaringClass().getSimpleName() + ")";
    }

    /*
     * Methods
     */
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

    static String getMethodDisplayText(Method method) {
        return method.getName() + " (" + method.getDeclaringClass().getSimpleName() + ")";
    }

    /*
     * Completion Suggestions
     */
    static <T> Map<CompletionSuggestionIF, Integer> createRatedSuggestions(List<T> objects, Function<T, CompletionSuggestionIF> suggestionBuilder, ToIntFunction<T> ratingFunc) {
		Map<CompletionSuggestionIF, Integer> ratedSuggestions = new LinkedHashMap<>();
    	for (T object : objects) {
			CompletionSuggestionIF suggestion = suggestionBuilder.apply(object);
			int rating = ratingFunc.applyAsInt(object);
			ratedSuggestions.put(suggestion, rating);
		}
		return ratedSuggestions;
    }
}
