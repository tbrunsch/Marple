package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;

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

    static final int CLASS_MATCH_FULL                   = 0;
    static final int CLASS_MATCH_INHERITANCE            = 1;
    static final int CLASS_MATCH_PRIMITIVE_CONVERSION   = 2;
    static final int CLASS_MATCH_BOXED                  = 3;
    static final int CLASS_MATCH_BOXED_AND_CONVERSION   = 4;
    static final int CLASS_MATCH_BOXED_AND_INHERITANCE  = 5;
    static final int CLASS_MATCH_NONE                   = 6;

    static int rateClassMatch(Class<?> actual, Class<?> expected) {
    	if (expected == null) {
    		// no expectations
			return CLASS_MATCH_FULL;
		}

		if (actual == null) {
			// null object (only object without class) is convertible to any non-primitive class
			return expected.isPrimitive() ? CLASS_MATCH_NONE : CLASS_MATCH_FULL;
		}

        if (actual == expected) {
            return CLASS_MATCH_FULL;
        }

		boolean primitiveConvertible = ReflectionUtils.isPrimitiveConvertibleTo(actual, expected);
		if (expected.isPrimitive()) {
			if (actual.isPrimitive()) {
        		return primitiveConvertible
						? CLASS_MATCH_PRIMITIVE_CONVERSION	// int -> double
						: CLASS_MATCH_NONE;					// int -> boolean
			} else {
				Class<?> actualUnboxed = ReflectionUtils.getPrimitiveClass(actual);
				return	actualUnboxed == expected	? CLASS_MATCH_BOXED :				// Integer -> int
						primitiveConvertible		? CLASS_MATCH_BOXED_AND_CONVERSION	// Integer -> double
													: CLASS_MATCH_NONE;					// Integer -> boolean
			}
		} else {
			if (actual.isPrimitive()) {
				Class<?> actualBoxed = ReflectionUtils.getBoxedClass(actual);
				return	actualBoxed == expected					? CLASS_MATCH_BOXED :					// int -> Integer
						primitiveConvertible					? CLASS_MATCH_BOXED_AND_CONVERSION :	// int -> Double
						expected.isAssignableFrom(actualBoxed)	? CLASS_MATCH_BOXED_AND_INHERITANCE		// int -> Number
																: CLASS_MATCH_NONE;						// int -> String
			} else {
				return	expected.isAssignableFrom(actual)	? CLASS_MATCH_INHERITANCE	// Integer -> Number
															: CLASS_MATCH_NONE;			// Integer -> Double
			}
		}
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

    static ToIntFunction<Field> rateFieldByClassesFunc(final List<Class<?>> expectedClasses) {
        return field -> rateFieldByClasses(field, expectedClasses);
    }

    private static int rateFieldByClasses(Field field, List<Class<?>> expectedClasses) {
        return	expectedClasses == null		? CLASS_MATCH_FULL :
				expectedClasses.isEmpty()	? CLASS_MATCH_NONE
											: expectedClasses.stream().mapToInt(expectedClass -> rateClassMatch(field.getType(), expectedClass)).min().getAsInt();
    }

    static ToIntFunction<Field> rateFieldByNameAndClassesFunc(final String fieldName, final List<Class<?>> expectedClasses) {
        return field -> (CLASS_MATCH_NONE +1)*rateFieldByName(field, fieldName) + rateFieldByClasses(field, expectedClasses);
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

    static ToIntFunction<Method> rateMethodByClassesFunc(final List<Class<?>> expectedClasses) {
        return method -> rateMethodByClasses(method, expectedClasses);
    }

    private static int rateMethodByClasses(Method method, List<Class<?>> expectedClasses) {
        return	expectedClasses == null		? CLASS_MATCH_FULL :
				expectedClasses.isEmpty()	? CLASS_MATCH_NONE
											: expectedClasses.stream().mapToInt(expectedClass -> rateClassMatch(method.getReturnType(), expectedClass)).min().getAsInt();
    }

    static ToIntFunction<Method> rateMethodByNameAndClassesFunc(final String methodName, final List<Class<?>> expectedClasses) {
        return method -> (CLASS_MATCH_NONE +1)*rateMethodByName(method, methodName) + rateMethodByClasses(method, expectedClasses);
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
