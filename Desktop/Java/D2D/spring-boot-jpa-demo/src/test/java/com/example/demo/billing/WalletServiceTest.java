package com.example.demo.billing;

import com.example.demo.orders.shipment.Shipment;
import com.example.demo.userregistration.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private B2BClientRepository b2bClientRepository;

    @Mock
    private ClientWalletRepository clientWalletRepository;

    @Mock
    private WalletTransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    private User mockUser;
    private B2BClient mockClient;
    private ClientWallet mockWallet;
    private Shipment mockShipment;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId("usr-123");

        mockClient = new B2BClient();
        mockClient.setId(1L);
        mockClient.setUser(mockUser);

        mockWallet = new ClientWallet();
        mockWallet.setId(1L);
        mockWallet.setB2bClient(mockClient);
        mockWallet.setBalance(new BigDecimal("0.00"));
        mockWallet.setCreditLimit(new BigDecimal("1000.00"));

        mockShipment = new Shipment();
        mockShipment.setId(1L);
        mockShipment.setTrackingNumber("TRK-TEST");
    }

    @Test
    void testChargeWalletForShipment_Success() {
        when(clientWalletRepository.findByB2bClient(mockClient)).thenReturn(Optional.of(mockWallet));

        BigDecimal chargeAmount = new BigDecimal("50.00");
        walletService.chargeWalletForShipment(mockClient, mockShipment, chargeAmount);

        // Verify balance was reduced
        assertEquals(new BigDecimal("-50.00"), mockWallet.getBalance());
        
        verify(clientWalletRepository).save(mockWallet);

        ArgumentCaptor<WalletTransaction> transactionCaptor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        WalletTransaction capturedTransaction = transactionCaptor.getValue();
        assertEquals(WalletTransaction.TransactionType.DEBIT, capturedTransaction.getTransactionType());
        assertEquals(chargeAmount, capturedTransaction.getAmount());
        assertEquals(mockShipment, capturedTransaction.getShipment());
        assertFalse(capturedTransaction.isInvoiced());
    }

    @Test
    void testChargeWalletForShipment_CreditLimitExceeded() {
        mockWallet.setBalance(new BigDecimal("-950.00"));
        when(clientWalletRepository.findByB2bClient(mockClient)).thenReturn(Optional.of(mockWallet));

        BigDecimal chargeAmount = new BigDecimal("100.00"); // Pushes to -1050, exceeding 1000 limit

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            walletService.chargeWalletForShipment(mockClient, mockShipment, chargeAmount);
        });

        assertTrue(exception.getMessage().contains("Credit limit exceeded"));
        verify(clientWalletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}
