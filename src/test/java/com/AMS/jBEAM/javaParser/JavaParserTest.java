package com.AMS.jBEAM.javaParser;

import org.junit.Test;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JavaParserTest
{
    @Test
    public void testOnlyFieldCompletion() {
        AbstractClassUnderTest testInstance = new ConcreteClassUnderTest();
        JavaParser parser = new JavaParser();
        Map<String, List<String>> expectedResults = new LinkedHashMap<>();
        expectedResults.put("xy",   Arrays.asList("xy", "xyzw", "xy_z"));
        expectedResults.put("XY",   Arrays.asList("xy", "xyzw", "xy_z"));
        expectedResults.put("xy_z", Arrays.asList("xy_z", "xy"));
        expectedResults.put("XYZ",  Arrays.asList("xyzw", "xy"));
        expectedResults.put("xyzw", Arrays.asList("xyzw"));
        expectedResults.put("XYZW", Arrays.asList("xyzw"));
        expectedResults.put("a",    Arrays.asList("ab"));
        expectedResults.put("ab",   Arrays.asList("ab", "AB", "ABC"));
        expectedResults.put("AB",   Arrays.asList("AB", "ab", "ABC"));
        expectedResults.put("abc",  Arrays.asList("ABC", "ab", "AB"));
        expectedResults.put("ABC",  Arrays.asList("ABC", "AB", "ab"));
        expectedResults.put("abcd", Arrays.asList("ab"));
        for (Map.Entry<String, List<String>> expectedResult : expectedResults.entrySet()) {
            String javaExpression = expectedResult.getKey();
            List<String> expectedCompletionOrder = expectedResult.getValue();
            int carret = javaExpression.length();
            List<CompletionSuggestionIF> completions = parser.suggestCodeCompletion(javaExpression, carret, testInstance);
            assertTrue(MessageFormat.format("Expression: {0}, expected completions: {1}, actual completions: {2}",
                        javaExpression,
                        expectedCompletionOrder,
                        completions.stream().map(CompletionSuggestionIF::getSuggestion).collect(Collectors.toList())),
                    completions.size() > expectedCompletionOrder.size());
            for (int i = 0; i < expectedCompletionOrder.size(); i++) {
                assertEquals("Expression: " + javaExpression, expectedCompletionOrder.get(i), completions.get(i).getSuggestion());
            }
        }

    }

    private static abstract class AbstractClassUnderTest
    {
        private final static int    xy      = 12;
        private final static String xy_z    = "xyz";
        private final double        ab      = 2.4;
        private final float         AB      = -7.8f;
        private final long          ABC     = 27;
    }

    private static class ConcreteClassUnderTest extends AbstractClassUnderTest
    {
        private final char          xyzw    = '!';
    }
}
