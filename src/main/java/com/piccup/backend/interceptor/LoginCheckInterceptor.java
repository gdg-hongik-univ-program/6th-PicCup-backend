package com.piccup.backend.interceptor;

import com.piccup.backend.exception.BusinessException;
import com.piccup.backend.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 프론트엔드 연동을 위한 CORS Preflight (OPTIONS) 요청은 통과
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 기존 세션이 있으면 가져오고, 없으면 새로 만들지 않고 null 반환
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("LOGIN_USER_ID") == null) {
            log.warn("미인증 사용자의 API 요청 접근 - URI: {}", request.getRequestURI());
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }

        return true; // 세션이 존재하면 정상적으로 컨트롤러로 요청 전달
    }
}