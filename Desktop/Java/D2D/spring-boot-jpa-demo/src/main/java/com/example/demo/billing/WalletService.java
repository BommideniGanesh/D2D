package com.example.demo.billing;

import com.example.demo.orders.shipment.Shipment;
import com.example.demo.userregistration.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class WalletService {

    private final B2BClientRepository b2bClientRepository;
    private final ClientWalletRepository clientWalletRepository;
    private final WalletTransactionRepository transactionRepository;

    public WalletService(B2BClientRepository b2bClientRepository,
                         ClientWalletRepository clientWalletRepository,
                         WalletTransactionRepository transactionRepository) {
        this.b2bClientRepository = b2bClientRepository;
        this.clientWalletRepository = clientWalletRepository;
        this.transactionRepository = transactionRepository;
    }

    public Optional<B2BClient> getB2BClient(User user) {
        return b2bClientRepository.findByUser(user);
    }

    @Transactional
    public void chargeWalletForShipment(B2BClient client, Shipment shipment, BigDecimal amount) {
        ClientWallet wallet = clientWalletRepository.findByB2bClient(client)
                .orElseThrow(() -> new RuntimeException("Wallet not found for B2B Client"));

        // Deduct from balance
        wallet.setBalance(wallet.getBalance().subtract(amount));
        
        // Basic credit limit check (balance is negative, limit is positive. e.g. -10000 limit)
        if (wallet.getBalance().compareTo(wallet.getCreditLimit().negate()) < 0) {
            throw new RuntimeException("Credit limit exceeded. Cannot charge shipment to wallet.");
        }

        clientWalletRepository.save(wallet);

        // Record Transaction
        WalletTransaction transaction = WalletTransaction.builder()
                .wallet(wallet)
                .shipment(shipment)
                .transactionType(WalletTransaction.TransactionType.DEBIT)
                .amount(amount)
                .description("Shipment #" + shipment.getTrackingNumber() + " routing charge.")
                .isInvoiced(false)
                .build();
                
        transactionRepository.save(transaction);
    }
}
