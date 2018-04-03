package com.poetry.hearing.filter;

import com.poetry.hearing.requestCached.HttpSessionRequestCache;
import com.poetry.hearing.util.Constant;
import com.poetry.hearing.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class LoginFilter implements Filter {

    @Value("${token.header}")
    private String tokenHeader;

    @Autowired
    private TokenUtils tokenUtils;

    private HttpSessionRequestCache requestCache;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        requestCache = new HttpSessionRequestCache();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        System.out.println("doFilter...");
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        HttpSession session = request.getSession(false);
        if (session == null) {
            session = request.getSession(true);
        }
        if (session.getAttribute(Constant.SESSION_USER) != null) {
            filterChain.doFilter(request, servletResponse);
            return;
        }

        String authToken = request.getHeader(this.tokenHeader);
        if (authToken != null) {
            String username = this.tokenUtils.getUsernameFromToken(authToken);
            String password = this.tokenUtils.getPasswordFromToken(authToken);

            // 如果上面解析 token 成功并且拿到了 username 且token有效
            if (username != null && password != null && this.tokenUtils.validateToken(authToken)) {
                System.out.println("validate token!");
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                requestCache.saveRequest(request, response);
                response.sendRedirect("registerOrLogin");
            }
        } else {
            requestCache.saveRequest(request, response);
            response.sendRedirect("registerOrLogin");
        }
    }

    @Override
    public void destroy() {
    }
}
