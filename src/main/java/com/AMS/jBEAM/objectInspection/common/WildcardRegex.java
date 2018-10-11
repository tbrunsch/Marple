package com.AMS.jBEAM.objectInspection.common;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class WildcardRegex
{
    private static final Set<Character> SPECIAL_REGEX_CHARACTERS    = new HashSet<>();

    static {
        String specialCharacters = "\\.[]{}()<>+-=?^$|*";
        for (int i = 0; i < specialCharacters.length(); i++) {
            SPECIAL_REGEX_CHARACTERS.add(specialCharacters.charAt(i));
        }
    }

    public static Pattern createRegexPattern(String wildcardString) {
        StringBuilder regexBuilder = new StringBuilder();
        int numChars = wildcardString.length();
        for (int i = 0; i < numChars; i++) {
            char c = wildcardString.charAt(i);

            if (c == '*') {
                // wild card
                regexBuilder.append(".*");
            } else if (SPECIAL_REGEX_CHARACTERS.contains(c)) {
                // escape character
                regexBuilder.append("\\" + c);
            } else if (Character.isUpperCase(c)) {
                /*
                 * Insert wild cards before upper-case characters (except for the first character) as known from common IDEs
                 *
                 * Example: "ArrLi" will should also match "ArrayList", but not "xArrLi", so the corresponding regex should be "Arr[a-z0-9]*Li"
                 */
                if (i > 0) {
                    regexBuilder.append("[a-z0-9]*");
                }
                regexBuilder.append(c);
            } else {
                // ordinary character requires no special treatment
                regexBuilder.append(c);
            }
        }
        // wild card at the end to allow arbitrary suffixes
        regexBuilder.append(".*");
        return Pattern.compile(regexBuilder.toString());
    }
}
