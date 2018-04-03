package com.poetry.hearing.requestCached;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface RequestCache {
    void saveRequest(HttpServletRequest var1, HttpServletResponse var2);

    SavedRequest getRequest(HttpServletRequest var1, HttpServletResponse var2);

    HttpServletRequest getMatchingRequest(HttpServletRequest var1, HttpServletResponse var2);

    void removeRequest(HttpServletRequest var1, HttpServletResponse var2);
}
