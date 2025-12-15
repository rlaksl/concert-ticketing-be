package com.ticket.domain.user.repository;

import com.ticket.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 이메일 중복 체크
    boolean existsByEmail (String email);

    // 핸드폰 중복 체크
    boolean existsByPhone(String phone);
}
