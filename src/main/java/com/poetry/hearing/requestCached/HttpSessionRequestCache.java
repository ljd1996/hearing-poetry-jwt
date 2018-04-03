package com.poetry.hearing.requestCached;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HttpSessionRequestCache implements RequestCache {
    static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";
    protected final Log logger = LogFactory.getLog(this.getClass());
    private PortResolver portResolver = new PortResolverImpl();
    private boolean createSessionAllowed = true;
    private RequestMatcher requestMatcher;
    private String sessionAttrName;

    public HttpSessionRequestCache() {
        this.requestMatcher = AnyRequestMatcher.INSTANCE;
        this.sessionAttrName = "SPRING_SECURITY_SAVED_REQUEST";
    }

    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        if (this.requestMatcher.matches(request)) {
            DefaultSavedRequest savedRequest = new DefaultSavedRequest(request, this.portResolver);
            if (this.createSessionAllowed || request.getSession(false) != null) {
                request.getSession().setAttribute(this.sessionAttrName, savedRequest);
                this.logger.debug("DefaultSavedRequest added to Session: " + savedRequest);
            }
        } else {
            this.logger.debug("Request not saved as configured RequestMatcher did not match");
        }

    }

    public SavedRequest getRequest(HttpServletRequest currentRequest, HttpServletResponse response) {
        HttpSession session = currentRequest.getSession(false);
        return session != null ? (SavedRequest)session.getAttribute(this.sessionAttrName) : null;
    }

    public void removeRequest(HttpServletRequest currentRequest, HttpServletResponse response) {
        HttpSession session = currentRequest.getSession(false);
        if (session != null) {
            this.logger.debug("Removing DefaultSavedRequest from session if present");
            session.removeAttribute(this.sessionAttrName);
        }

    }

    public HttpServletRequest getMatchingRequest(HttpServletRequest request, HttpServletResponse response) {
        DefaultSavedRequest saved = (DefaultSavedRequest)this.getRequest(request, response);
        if (saved == null) {
            return null;
        } else if (!saved.doesRequestMatch(request, this.portResolver)) {
            this.logger.debug("saved request doesn't match");
            return null;
        } else {
            this.removeRequest(request, response);
            return new SavedRequestAwareWrapper(saved, request);
        }
    }

    public void setRequestMatcher(RequestMatcher requestMatcher) {
        this.requestMatcher = requestMatcher;
    }

    public void setCreateSessionAllowed(boolean createSessionAllowed) {
        this.createSessionAllowed = createSessionAllowed;
    }

    public void setPortResolver(PortResolver portResolver) {
        this.portResolver = portResolver;
    }

    public void setSessionAttrName(String sessionAttrName) {
        this.sessionAttrName = sessionAttrName;
    }
}
