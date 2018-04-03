package com.poetry.hearing.requestCached;

import javax.servlet.http.HttpServletRequest;

public interface RequestMatcher {
    boolean matches(HttpServletRequest var1);
}
