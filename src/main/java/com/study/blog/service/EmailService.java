package com.study.blog.service;

import com.study.blog.dto.email.EmailRequestDto;
import com.study.blog.exception.NotExistUserException;
import com.study.blog.exception.NotValidateEmailCode;
import com.study.blog.exception.NotValidateResetToken;
import com.study.blog.repository.UsersRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate stringRedisTemplate;
    private final UsersRepository usersRepository;

    private static final SecureRandom secureRandom = new SecureRandom();
//    private static final String senderEmail= "solvelog@gmail.com"; //회신 안되는 이메일

    private String emailCodeKey(String email) {return "ec:email:" + email; }
    private String resetTokenEmailKey(String email) {return "rp:email:" + email; }
    private String resetTokenKey(String token) {return "ro:token:"+token; }

    // 이메일 코드 생성 및 전송
    public void createAuthEmail (EmailRequestDto emailRequestDto) {
        String newEmailCode = createEmailCode(emailRequestDto);
//        SimpleMailMessage simpleMailMessage = new SimpleMailMessage(); // html 형식은 사용불가

        //html 형식 내용 사용 가능
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        String title = "[SolveLog] 이메일 인증 코드 안내";
        String content = "<html>"
            + "<body>"
            + "<h1>SolveLog 인증 코드 : "+newEmailCode+ "</h1>"
            + "<p>해당 코드를 홈페이지 입력하세요.</p>"
            + "<footer style='color: grey; font-size: small;'>"
            + "<p>※본 메일은 자동응답 메일이므로 본 메일에 회신하지 마시기 바랍니다</p>"
            + "</footer>"
            + "</body>"
            + "</html>"
            ;

        try {
            helper.setTo(emailRequestDto.getEmail());
            helper.setSubject(title);
            helper.setText(content, true);
            helper.setFrom("no-reply@solvelog.site");
            helper.setReplyTo("no-reply@solvelog.site");

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }

    //이메일 코드 유효성 검사
    public void validateEmailCode(EmailRequestDto emailRequestDto) {
        String authCode = emailRequestDto.getAuthCode();
        String email = emailRequestDto.getEmail();
//        validateEmailCodeRedis()
        if (!validateEmailCodeRedis(email, authCode)) {
            throw new NotValidateEmailCode();
        }

        stringRedisTemplate.delete(emailCodeKey(email));
    }

    // 비밀번호 재설정 이메일 발송
    @Value("${app.base-url}")
    private String baseUrl;
    public void sendResetPasswordEmail(String username) {
        if (!usersRepository.existsByUsername(username)) {
            throw new NotExistUserException();
        }

        // 현재 시간 설정
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedNow = now.format(formatter);

        String token = createResetToken(username);

        // 비밀번호 변경 링크
        String resetLink = baseUrl + "/resetPw?token=" + token;

        // 이메일 메시지 구성
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        try {
            helper.setTo(username);
            helper.setFrom("no-reply@solvelog.site");
            helper.setReplyTo("no-reply@solvelog.site");
            helper.setSubject("[SolveLog] 비밀번호 재설정 메일");

            // HTML 메시지 설정
            String htmlMsg = "<p>안녕하세요.</p>" +
                "<p>회원님은 " + formattedNow + "에 비밀번호 찾기 요청을 하셨습니다.</p>" +
                "<p>아래 버튼을 통해 비밀번호를 초기화해주세요.</p>" +
                "<p>회원 아이디: " + username + "</p>" +
                "<a href='" + resetLink + "' style='color: white; text-decoration: none; padding: 10px 20px; background-color: #1a73e8; border-radius: 5px; display: inline-block;'>비밀번호 변경</a>";

            // HTML 형식으로 메시지를 설정합니다.
            helper.setText(htmlMsg, true);

            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send auth email to "+username, e);
        }

    }

    // 이메일 검증용 코드 생성
    public String createEmailCode(EmailRequestDto emailRequestDto) {
        int number = secureRandom.nextInt(1_000_000);  // 0~999999
        String newEmailCode = String.format("%06d", number);// 항상 6자리

        String email = emailRequestDto.getEmail();

        // opsForValue.set(...) 은 덮어쓰기라 아래 코드는 무용지물
//        if (validateRedisKey(emailCodeKey(email))) {
//            // 재전송 전송제한 둘려면 여기
//            deleteEmailCodeRedis(email);
//            saveEmailCodeRedis(newEmailCode, email);
//        } else {
//            saveEmailCodeRedis(newEmailCode, email);
//        }

        saveEmailCodeRedis(newEmailCode, email);

        return newEmailCode;
    }

    // 토큰으로 이메일 조회
    public String getEmailByToken(String token) {
        if (!validateResetToken(token)) throw new NotValidateResetToken();
        return getEmailByResetTokenRedis(token);
    }

    public void deleteResetToken(String email, String token) {
        deleteResetTokenRedis(email,token);
    }

//======= Redis
    //redis 에 작성된 코드 저장
    private void saveEmailCodeRedis(String emailCode, String email) {
        stringRedisTemplate.opsForValue().set(
            emailCodeKey(email),
            emailCode,
            Duration.ofMinutes(5)
        );
    }

    // 작성된 이메일 코드 삭제
    private void deleteEmailCodeRedis(String email) {
        stringRedisTemplate.delete(emailCodeKey(email));
    }

    // redis key 값 검사
    private boolean validateRedisKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    //redis 이메일 코드 유효성 검사
    private boolean validateEmailCodeRedis(String email, String emailCode) {
        String saved = stringRedisTemplate.opsForValue().get(emailCodeKey(email));
        return saved != null && saved.equals(emailCode);
    }

    // 비밀번호 재설정 토큰 생성 및 redis 저장
    private String createResetToken(String email) {
        String token = UUID.randomUUID().toString();

        String oldResetKey = getResetTokenByEmail(email);

        if (oldResetKey != null) {
            deleteResetTokenRedis(email, oldResetKey);
        }

        stringRedisTemplate.opsForValue().set(
            resetTokenEmailKey(email),
            token,
            Duration.ofHours(1)
        );

        stringRedisTemplate.opsForValue().set(
            resetTokenKey(token),
            email,
            Duration.ofHours(1)
        );

        return token;
    }

    // 토큰으로 이메일 조회
    private String getEmailByResetTokenRedis(String token) {
        return stringRedisTemplate.opsForValue().get(resetTokenKey(token));
    }

    // 이메일로 토큰 조회
    private String getResetTokenByEmail(String email) {
        return stringRedisTemplate.opsForValue().get(resetTokenEmailKey(email));
    }

    // 재설정 토큰 유효성 검사
    private boolean validateResetToken(String token) {
        return stringRedisTemplate.hasKey(resetTokenKey(token));
    }

    // 작성된 재설정 코드 삭제
    private void deleteResetTokenRedis(String email, String token) {
        stringRedisTemplate.delete(resetTokenEmailKey(email));
        stringRedisTemplate.delete(resetTokenKey(token));
    }

}
