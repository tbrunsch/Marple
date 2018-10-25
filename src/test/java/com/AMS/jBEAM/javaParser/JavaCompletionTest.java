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

public class JavaCompletionTest
{
    @Test
    public void testField() {
        new TestBuilder()
            .addTest("xy",   "xy", "xyzw", "xy_z")
            .addTest("XY",   "xy", "xyzw", "xy_z")
            .addTest("xy_z", "xy_z", "xy")
            .addTest("XYZ",  "xyzw", "xy")
            .addTest("xyzw", "xyzw")
            .addTest("XYZW", "xyzw")
            .addTest("a",    "ab")
            .addTest("ab",   "ab", "AB", "ABC")
            .addTest("AB",   "AB", "ab", "ABC")
            .addTest("abc",  "ABC", "ab", "AB")
            .addTest("ABC",  "ABC", "AB", "ab")
            .addTest("abcd", "ab")
            .performTests();
    }

    @Test
    public void testFieldDot() {
        AbstractClassUnderTest testInstance = new ConcreteClassUnderTest();
        JavaParser parser = new JavaParser();
        String javaExpression = "member.";
        List<String> expectedCompletions = Arrays.asList("xy", "xy_z", "ab", "AB", "ABC", "xyzw", "member");
        List<String> suggestions = null;
        try {
            suggestions = extractSuggestions(parser.suggestCodeCompletion(javaExpression, javaExpression.length(), testInstance));
        } catch (JavaParseException e) {
            assertTrue("Exception during code completion: " + e.getMessage(), false);
        }
        for (String expectedCompletion : expectedCompletions) {
            assertTrue("Expected completion '" + expectedCompletion + "'", suggestions.contains(expectedCompletion));
        }
    }

    @Test
    public void testFieldDotField() {
        new TestBuilder()
            .addTest("member.xy",   "xy", "xyzw", "xy_z")
            .addTest("member.XY",   "xy", "xyzw", "xy_z")
            .addTest("member.xy_z", "xy_z", "xy")
            .addTest("member.XYZ",  "xyzw", "xy")
            .addTest("member.xyzw", "xyzw")
            .addTest("member.XYZW", "xyzw")
            .addTest("member.a",    "ab")
            .addTest("member.ab",   "ab", "AB", "ABC")
            .addTest("member.AB",   "AB", "ab", "ABC")
            .addTest("member.abc",  "ABC", "ab", "AB")
            .addTest("member.ABC",  "ABC", "AB", "ab")
            .addTest("member.abcd", "ab")
            .performTests();
    }

    private static List<String> extractSuggestions(List<CompletionSuggestion> completions) {
        return completions.stream()
            .map(completion -> completion.getInsertionInfo().getTextToInsert())
            .collect(Collectors.toList());
    }

    private static class TestBuilder
    {
        private final Map<String, List<String>> expectedResults = new LinkedHashMap<>();

        TestBuilder addTest(String javaExpression, String... expectedSuggestions) {
            expectedResults.put(javaExpression, Arrays.asList(expectedSuggestions));
            return this;
        }

        void performTests() {
            AbstractClassUnderTest testInstance = new ConcreteClassUnderTest();
            JavaParser parser = new JavaParser();
            for (Map.Entry<String, List<String>> expectedResult : expectedResults.entrySet()) {
                String javaExpression = expectedResult.getKey();
                List<String> expectedSuggestions = expectedResult.getValue();
                int caret = javaExpression.length();
                List<String> suggestions = null;
                try {
                    suggestions = extractSuggestions(parser.suggestCodeCompletion(javaExpression, caret, testInstance));
                } catch (JavaParseException e) {
                    assertTrue("Exception during code completion: " + e.getMessage(), false);
                }
                assertTrue(MessageFormat.format("Expression: {0}, expected completions: {1}, actual completions: {2}",
                        javaExpression,
                        expectedSuggestions,
                        suggestions),
                        suggestions.size() > expectedSuggestions.size());
                for (int i = 0; i < expectedSuggestions.size(); i++) {
                    assertEquals("Expression: " + javaExpression, expectedSuggestions.get(i), suggestions.get(i));
                }
            }
        }
    }

    private static abstract class AbstractClassUnderTest
    {
        private final static int    xy      = 12;
        private final static String xy_z    = "xyz";
        private final double        ab      = 2.4;
        private float               AB      = -7.8f;
        private final long          ABC     = 27;
    }

    private static class ConcreteClassUnderTest extends AbstractClassUnderTest
    {
        private final char              xyzw    = '!';
        private ConcreteClassUnderTest  member  = null;
    }
}
