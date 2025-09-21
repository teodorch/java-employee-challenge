package com.reliaquest.api.controller.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import static java.util.stream.Collectors.joining;

@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String params = request.getParameterMap()
                .entrySet()
                .stream()
                .map(entry -> String.format("%s=%s", entry.getKey(),
                        String.join(",", entry.getValue())))
                .collect(joining("&"));

        log.info(
                "Received request: {} {} {} from {}",
                request.getMethod(),
                request.getRequestURI(),
                params,
                request.getRemoteAddr());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        log.info(
                "Sent response: {} {} with status {}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                ex);
    }
}
