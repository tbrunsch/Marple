package com.AMS.jBEAM.objectInspection.common;

public class DisplayUtils
{
    private static final String NULL_STRING = "(null)";

    public static String toString(Object o) {
        return o == null ? NULL_STRING : o.toString();
    }
}
