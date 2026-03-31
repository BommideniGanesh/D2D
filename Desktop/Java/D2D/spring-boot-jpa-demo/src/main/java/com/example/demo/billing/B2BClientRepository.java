package com.example.demo.billing;

import com.example.demo.userregistration.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface B2BClientRepository extends JpaRepository<B2BClient, Long> {
    Optional<B2BClient> findByUser(User user);
}
