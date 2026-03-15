package com.mycoinex.exchange.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycoinex.exchange.dto.ErrorResponse;
import com.mycoinex.exchange.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    private final ObjectMapper objectMapper;

    public AdminInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        User user = AuthContext.getCurrentUser();
        if (user == null || !user.isAdmin()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse("需要管理员权限。")));
            return false;
        }
        return true;
    }
}
