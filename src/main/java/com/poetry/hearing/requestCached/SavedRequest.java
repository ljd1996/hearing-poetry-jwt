package com.poetry.hearing.requestCached;

import javax.servlet.http.Cookie;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface SavedRequest extends Serializable {
    String getRedirectUrl();

    List<Cookie> getCookies();

    String getMethod();

    List<String> getHeaderValues(String var1);

    Collection<String> getHeaderNames();

    List<Locale> getLocales();

    String[] getParameterValues(String var1);

    Map<String, String[]> getParameterMap();
}
