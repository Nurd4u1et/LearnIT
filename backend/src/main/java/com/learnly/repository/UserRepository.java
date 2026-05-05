package com.learnly.repository;

import com.learnly.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.*;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
