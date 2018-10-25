package com.AMS.jBEAM.javaParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

class CompletionUtils
{
    /*
     * String Comparison
     */
    private static final int FULL_MATCH                         = 0;
    private static final int FULL_MATCH_IGNORE_CASE             = 1;
    private static final int PREFIX_MATCH                       = 2;
    private static final int PREFIX_MATCH_IGNORE_CASE           = 3;
    private static final int INVERSE_PREFIX_MATCH               = 4;
    private static final int INVERSE_PREFIX_MATCH_IGNORE_CASE   = 5;
    private static final int MIN_VALUE_OTHER                    = 6;

    private static int rateStringMatch(String actual, String expected) {
        if (actual.equals(expected)) {
            return FULL_MATCH;
        } else {
            String actualLowerCase = actual.toLowerCase();
            String expectedLowerCase = expected.toLowerCase();
            if (actualLowerCase.equals(expectedLowerCase)) {
                return FULL_MATCH_IGNORE_CASE;
            } else if (actual.startsWith(expected)) {
                return PREFIX_MATCH;
            } else if (actualLowerCase.startsWith(expectedLowerCase)) {
                return PREFIX_MATCH_IGNORE_CASE;
            } else if (expected.startsWith(actual)) {
                return INVERSE_PREFIX_MATCH;
            } else if (expectedLowerCase.startsWith(actualLowerCase)) {
                return INVERSE_PREFIX_MATCH_IGNORE_CASE;
            } else {
                // TODO: Differentiate between Strings
                return MIN_VALUE_OTHER;
            }
        }
    }

    /*
     * Fields
     */
    static final Function<Field, String>    FIELD_DISPLAY_FUNC          = CompletionUtils::getFieldDisplayText;

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
        return expectedClass.isAssignableFrom(field.getType()) ? 0 : 1;
    }

    static ToIntFunction<Field> rateFieldByNameAndClassFunc(final String fieldName, final Class<?> expectedClass) {
        return field -> 2*rateFieldByName(field, fieldName) + rateFieldByClass(field, expectedClass);
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
    static final Function<Method, String>   METHOD_DISPLAY_FUNC = CompletionUtils::getMethodDisplayText;

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
        return expectedClass.isAssignableFrom(method.getReturnType()) ? 0 : 1;
    }

    static ToIntFunction<Method> rateMethodyNameAndClassFunc(final String methodName, final Class<?> expectedClass) {
        return method -> 2*rateMethodByName(method, methodName) + rateMethodByClass(method, expectedClass);
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
