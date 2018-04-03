package com.poetry.hearing.requestCached;

import javax.servlet.http.HttpServletRequest;

public final class AnyRequestMatcher implements RequestMatcher {
    public static final RequestMatcher INSTANCE = new AnyRequestMatcher();

    public boolean matches(HttpServletRequest request) {
        return true;
    }

    public boolean equals(Object obj) {
        return obj instanceof AnyRequestMatcher || obj instanceof AnyRequestMatcher;
    }

    public int hashCode() {
        return 1;
    }

    private AnyRequestMatcher() {
    }
}
