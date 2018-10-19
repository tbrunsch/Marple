package com.AMS.jBEAM.javaParser;

class MatchValuer
{
    private static final int FULL_MATCH                         = 0;
    private static final int FULL_MATCH_IGNORE_CASE             = 1;
    private static final int PREFIX_MATCH                       = 2;
    private static final int PREFIX_MATCH_IGNORE_CASE           = 3;
    private static final int INVERSE_PREFIX_MATCH               = 4;
    private static final int INVERSE_PREFIX_MATCH_IGNORE_CASE   = 5;
    private static final int MIN_VALUE_OTHER                    = 6;

    static int rateStringMatch(String actual, String expected) {
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
}
