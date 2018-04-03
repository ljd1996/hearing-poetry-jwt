package com.poetry.hearing.requestCached;

import javax.servlet.http.Cookie;
import java.io.Serializable;

public class SavedCookie implements Serializable {
    private final String name;
    private final String value;
    private final String comment;
    private final String domain;
    private final int maxAge;
    private final String path;
    private final boolean secure;
    private final int version;

    public SavedCookie(String name, String value, String comment, String domain, int maxAge, String path, boolean secure, int version) {
        this.name = name;
        this.value = value;
        this.comment = comment;
        this.domain = domain;
        this.maxAge = maxAge;
        this.path = path;
        this.secure = secure;
        this.version = version;
    }

    public SavedCookie(Cookie cookie) {
        this(cookie.getName(), cookie.getValue(), cookie.getComment(), cookie.getDomain(), cookie.getMaxAge(), cookie.getPath(), cookie.getSecure(), cookie.getVersion());
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public String getComment() {
        return this.comment;
    }

    public String getDomain() {
        return this.domain;
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    public String getPath() {
        return this.path;
    }

    public boolean isSecure() {
        return this.secure;
    }

    public int getVersion() {
        return this.version;
    }

    public Cookie getCookie() {
        Cookie c = new Cookie(this.getName(), this.getValue());
        if (this.getComment() != null) {
            c.setComment(this.getComment());
        }

        if (this.getDomain() != null) {
            c.setDomain(this.getDomain());
        }

        if (this.getPath() != null) {
            c.setPath(this.getPath());
        }

        c.setVersion(this.getVersion());
        c.setMaxAge(this.getMaxAge());
        c.setSecure(this.isSecure());
        return c;
    }
}
