package com.study.blog.service;

import com.study.blog.dto.email.EmailRequestDto;
import com.study.blog.dto.file.FileResponseDto;
import com.study.blog.dto.users.ResetTokenDto;
import com.study.blog.dto.users.UserRequestDto;
import com.study.blog.dto.users.UsersResponseDto;
import com.study.blog.entity.File;
import com.study.blog.entity.Users;
import com.study.blog.entity.enums.Role;
import com.study.blog.exception.ExistUserException;
import com.study.blog.exception.NotExistUserException;
import com.study.blog.repository.FileRepository;
import com.study.blog.repository.UsersRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class UsersService implements UserDetailsService{

    private final UsersRepository usersRepository;
    private final FileRepository fileRepository;
    private final EmailService emailService;

    // 유저 리스트 관리자용 (페이지네이션 아직)
    public List<UsersResponseDto> getList() {

        return usersRepository.findAll().stream().map(
            user -> new UsersResponseDto(user.getId(), user.getNickname())
        ).toList();
    }

    public List<UsersResponseDto> getList(Pageable pageable) {
        return usersRepository.findAll(pageable).stream().map(
            user -> new UsersResponseDto(user.getId(), user.getNickname())
        ).toList();
    }


    // 유저 검색
    public UsersResponseDto getOne(String username) {
        Optional<Users> byId = usersRepository.findUsersByUsername(username);
        Users user = byId.orElseThrow();
        Optional<File> optionalUserImg = fileRepository.findUserImgByUsername(username);

        FileResponseDto fileResponseDto = optionalUserImg
            .map(img -> new FileResponseDto(img.getId(), img.getOriginalFileName(), img.getPath()))
            .orElse(null);

        return new UsersResponseDto(
            user.getId(),
            user.getNickname(),
            user.getBio(),
            fileResponseDto
        );
    }

    // 유저 생성
    @Transactional
    public Long createUser(UserRequestDto userRequestDto) {
        // 이메일 코드 인증
        emailService.validateEmailCode(
            new EmailRequestDto(userRequestDto.getUsername(), userRequestDto.getAuthCode()));

//        Boolean exist = usersRepository.existsByUsername(userRequestDto.getUsername());
//        if (exist) {
//            throw new ExistUserException();
//        }

        Optional<Users> optionalUser = usersRepository.findUsersByUsername(
            userRequestDto.getUsername());

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodePassword = bCryptPasswordEncoder.encode(userRequestDto.getPassword());

        if (optionalUser.isPresent()) { // 유저 존재 유무
            Users findUser = optionalUser.get();

            if ("y".equals(findUser.getIsDeleted())) {
                // 재가입 처리
                findUser.reCreateUser(encodePassword, userRequestDto.getNickName(),
                    userRequestDto.getBio(), Role.USER);
                return findUser.getId();
            } else {
                throw new ExistUserException();
            }
        }

        Users newUser = Users.createUser(userRequestDto.getUsername(), encodePassword,
            userRequestDto.getNickName(), Role.USER);

        usersRepository.save(newUser);
        return newUser.getId();
    }

    // 유저 수정
    @Transactional
    public UsersResponseDto updateUser(UserRequestDto userRequestDto) {
//        Users findUser = usersRepository.findById(userRequestDto.getUserId()).orElseThrow();
        Users findUser = usersRepository.findUsersByUsername(userRequestDto.getUsername())
            .orElseThrow();


        // 닉네임 변경
        if (userRequestDto.getNickName() != null) {
            findUser.updateUser(
                userRequestDto.getNickName(),
                findUser.getBio(),
                userRequestDto.getRole()
            );
        }

        // 바이오 변경
        if (userRequestDto.getBio() != null) {
            findUser.updateUser(
                findUser.getNickname(),
                userRequestDto.getBio(),
                userRequestDto.getRole()
            );
        }

        return new UsersResponseDto(findUser.getId(), findUser.getNickname());
    }

    // 유저 삭제
    @Transactional
    public void deleteUser(String username) {
        Users findUser = usersRepository.findUsersByUsername(username)
            .orElseThrow(NotExistUserException::new);

        findUser.deleteUser();
    }

    // 유저 비밀번호 재설정
    @Transactional
    public void resetUserPassword(ResetTokenDto resetTokenDto) {
        // redis 에 저장된 토큰과 비교하여 username 가져옴 -> user를 찾고 비밀번호 변경
        String resetToken = resetTokenDto.getResetToken();
        String newPassword = resetTokenDto.getPassword();
        String email = resetTokenDto.getUsername();
//        String email = emailService.getEmailByToken(resetToken);

        Users findUser = usersRepository.findUsersByUsernameIsNotDeleted(email)
            .orElseThrow(NotExistUserException::new);

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encodePassword = bCryptPasswordEncoder.encode(newPassword);

        findUser.resetPassword(encodePassword);

        //토큰 삭제
        emailService.deleteResetToken(email,resetToken);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        Users findUser = usersRepository.findUsersByUsername(username)
//            .orElseThrow(NotExistUserException::new);
        Users findUser = usersRepository.findUsersByUsernameIsNotDeleted(username)
            .orElseThrow(NotExistUserException::new);

        return new CustomUserDetails(findUser);
    }

}
