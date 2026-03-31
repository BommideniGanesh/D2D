package com.example.demo.userregistration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRegistrationRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    java.util.Optional<User> findByEmail(String email);

    long countByIsDeletedFalse();

    java.util.List<User> findByIsDeletedFalse();
}
