package com.AMS.jBEAM.javaParser;

class JavaToken
{
    private final String    token;
    private final boolean   containsCarret;

    JavaToken(String token, boolean containsCarret) {
        this.token = token;
        this.containsCarret = containsCarret;
    }

    public String getToken() {
        return token;
    }

    public boolean isContainsCarret() {
        return containsCarret;
    }
}
