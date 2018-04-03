package com.poetry.hearing.requestCached;

import javax.servlet.ServletRequest;

public interface PortResolver {
    int getServerPort(ServletRequest var1);
}
