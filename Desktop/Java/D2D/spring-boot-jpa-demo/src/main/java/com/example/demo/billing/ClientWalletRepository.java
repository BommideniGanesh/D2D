package com.example.demo.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientWalletRepository extends JpaRepository<ClientWallet, Long> {
    Optional<ClientWallet> findByB2bClient(B2BClient b2bClient);
}
