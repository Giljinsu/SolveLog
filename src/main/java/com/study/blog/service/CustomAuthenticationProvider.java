package com.study.blog.service;

import com.study.blog.entity.Users;
import com.study.blog.service.login.LoginAttemptService;
import com.study.blog.utils.RequestContextHolderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UsersService usersService;
    private final LoginAttemptService attemptService;
    private final BCryptPasswordEncoder passwordEncoder;


    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {
        /*
            authenticate 메소드는 사용자의 인증을 시도하고, 성공 또는 실패 여부를 나타내는 Authentication 객체를 반환
         */
        String username = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();


        UserDetails userDetails = usersService.loadUserByUsername(username);

        // ip
//        String ip = RequestContextHolderUtils.getIp();
//        if (attemptService.isBlocked(ip)) {
//            throw new LockedException("login is locked");
//        }

        if (attemptService.isBlocked(username)) {
            throw new LockedException("login is locked");
        }


        if (!passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
//            attemptService.loginFailed(ip);
            attemptService.loginFailed(username);
//            throw new BadCredentialsException("the password doesn't match");
            throw new IllegalStateException("wrong password");
//            throw new BadCredentialsException("wrong password", null);
        }

//        attemptService.loginSucceeded(ip);
        attemptService.loginSucceeded(username);


        return new UsernamePasswordAuthenticationToken(
            userDetails, // userDetails를 넣어줘야 @AuthenticationPrincipal 을 사용 가능
            null,
            userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // supports 메소드는 특정 타입의 Authentication 객체를 지원하는지 확인하는 역할을 한다.
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
