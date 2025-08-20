package com.shop.userservice.controller.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class IpFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String ip = getClientIp(request);

        MDC.put("ip", ip);

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.remove("ip");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknow".equalsIgnoreCase(ip)) {
            return ip.split(",")[0];
        }
        ip = request.getHeader("X-Real-Ip");
        return (ip != null && !ip.isEmpty()) ? ip : request.getRemoteAddr();
    }
}
