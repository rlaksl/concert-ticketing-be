package com.ticket.domain.user.service;

import com.ticket.domain.user.dto.SignUpRequest;
import com.ticket.domain.user.dto.SignUpResponse;
import com.ticket.domain.user.repository.UserRepository;
import com.ticket.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() {
        // given
        SignUpRequest request = new SignUpRequest(
                "test@email.com",
                "pw123456",
                "홍길동",
                "010-1234-5678"
        );

        // when
        SignUpResponse response = userService.signUp(request);

        // then
        assertThat(response.email()).isEqualTo("test@email.com");
        assertThat(response.name()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("이메일 중복시 예외 발생")
    void signUp_duplicateEmail() {
        // given
        SignUpRequest request1 = new SignUpRequest(
                "test@email.com",
                "pw123456",
                "홍길동",
                "010-1234-5678"
        );
        userService.signUp(request1);

        SignUpRequest request2 = new SignUpRequest(
                "test@email.com",
                "pw123456",
                "김철수",
                "010-9999-8888"
        );

        // when & then
        assertThatThrownBy(() -> userService.signUp(request2))
                .isInstanceOf(CustomException.class);
    }
}