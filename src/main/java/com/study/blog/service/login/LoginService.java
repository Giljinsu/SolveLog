package com.study.blog.service.login;

import com.study.blog.dto.login.LoginRequestDto;
import com.study.blog.entity.Users;
import com.study.blog.exception.NotExistUserException;
import com.study.blog.repository.UsersRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/*
    현재 이 로그인 서비스는 스프링 시큐리티를 우회해서 사용하는 것
 */
// 사용 X
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LoginService {
    private final UsersRepository usersRepository;
    private final LoginAttemptService attemptService;
    private final BCryptPasswordEncoder passwordEncoder;

    // 로그인
    public boolean userLogin(LoginRequestDto loginRequestDto, HttpServletRequest request) {

        // 프록시(클라이언트와 서버 사이에서 데이터를 전달해 주는 서버) 거칠 경우
        String userIp = request.getHeader("X-Forwarded-For");

        if (userIp.isEmpty() || "unknown".equalsIgnoreCase(userIp)) {
            userIp = request.getRemoteAddr();
        }

        if (attemptService.isBlocked(userIp)) { // 제한 되었는지 체크
            throw new LockedException("exceed login attempt");
        }

        Users users = usersRepository.findUsersByUsername(loginRequestDto.getUsername())
            .orElseThrow(NotExistUserException::new);


        if (passwordEncoder.matches(loginRequestDto.getPassword(), users.getPassword())) {
            // 로그인 성공 후에도 인증된 이용자로 처리
            UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                    users.getUsername(),
                    null,
                    List.of(new SimpleGrantedAuthority(users.getRole().toString()))
                );

            SecurityContextHolder.getContext().setAuthentication(token);

            // 세션 저장
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());


            attemptService.loginSucceeded(userIp);
            return true;
        } else {
            attemptService.loginFailed(userIp);
            return false;
        }

    }

    //로그 아웃 처리도 직접 짜야됨
}
