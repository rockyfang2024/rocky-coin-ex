package com.mycoinex.exchange.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoinex.exchange.dto.ErrorResponse;
import com.mycoinex.exchange.model.User;
import com.mycoinex.exchange.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String token = resolveToken(request);
        if (token == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "请先登录。");
            return false;
        }
        User user = authService.authenticate(token);
        if (user == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "登录已失效，请重新登录。");
            return false;
        }
        AuthContext.setCurrentUser(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return request.getHeader("X-Auth-Token");
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(message)));
    }
}
