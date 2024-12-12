package com.dws.challenge;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepositoryInMemory;
import com.dws.challenge.service.NotificationService;
import com.dws.challenge.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferServiceTest {

    private TransferService transferService;
    private AccountsRepositoryInMemory accountsRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        accountsRepository = new AccountsRepositoryInMemory();
        notificationService = Mockito.mock(NotificationService.class);
        transferService = new TransferService(accountsRepository, notificationService);
    }

    @Test
    void transferSuccess() {
        Account accountFrom = new Account("A1", new BigDecimal("500.00"));
        Account accountTo = new Account("A2", new BigDecimal("200.00"));
        accountsRepository.createAccount(accountFrom);
        accountsRepository.createAccount(accountTo);

        transferService.transfer("A1", "A2", new BigDecimal("100.00"));

        assertThat(accountFrom.getBalance()).isEqualByComparingTo("400.00");
        assertThat(accountTo.getBalance()).isEqualByComparingTo("300.00");

        Mockito.verify(notificationService).notifyAboutTransfer(accountFrom, "Transferred 100.00 to account ID: A2");
        Mockito.verify(notificationService).notifyAboutTransfer(accountTo, "Received 100.00 from account ID: A1");
    }

    @Test
    void transferFailsForInsufficientBalance() {
        Account accountFrom = new Account("A1", new BigDecimal("50.00"));
        Account accountTo = new Account("A2", new BigDecimal("200.00"));
        accountsRepository.createAccount(accountFrom);
        accountsRepository.createAccount(accountTo);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transferService.transfer("A1", "A2", new BigDecimal("100.00"))
        );

        assertThat(exception.getMessage()).isEqualTo("Insufficient balance in the source account.");
    }

    @Test
    void transferFailsForInvalidAccounts() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transferService.transfer("Invalid1", "Invalid2", new BigDecimal("100.00"))
        );

        assertThat(exception.getMessage()).isEqualTo("One or both accounts do not exist.");
    }

    @Test
    void transferFailsForNegativeAmount() {
        Account accountFrom = new Account("A1", new BigDecimal("500.00"));
        Account accountTo = new Account("A2", new BigDecimal("200.00"));
        accountsRepository.createAccount(accountFrom);
        accountsRepository.createAccount(accountTo);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transferService.transfer("A1", "A2", new BigDecimal("-50.00"))
        );

        assertThat(exception.getMessage()).isEqualTo("Transfer amount must be positive.");
    }

    @Test
    void transferFailsForSameAccount() {
        Account account = new Account("A1", new BigDecimal("500.00"));
        accountsRepository.createAccount(account);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transferService.transfer("A1", "A1", new BigDecimal("100.00"))
        );

        assertThat(exception.getMessage()).isEqualTo("Cannot transfer money to the same account.");
    }
}
