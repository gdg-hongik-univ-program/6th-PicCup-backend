package com.piccup.backend.interceptor;

import com.piccup.backend.config.ActiveUserTracker;
import com.piccup.backend.exception.BusinessException;
import com.piccup.backend.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final ActiveUserTracker activeUserTracker;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 기존 세션이 있으면 가져오고, 없으면 새로 만들지 않고 null 반환
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("LOGIN_USER_ID") == null) {
            log.warn("미인증 사용자의 API 요청 접근 - URI: {}", request.getRequestURI());
            throw new BusinessException(ErrorCode.UNAUTHENTICATED);
        }

        // 세션 인증을 통과한 정상 유저라면, 활동 시간을 방금(now)으로 갱신
        Long userId = (Long) session.getAttribute("LOGIN_USER_ID");
        activeUserTracker.markUserActive(userId);

        return true; // 세션이 존재하면 정상적으로 컨트롤러로 요청 전달
    }
}