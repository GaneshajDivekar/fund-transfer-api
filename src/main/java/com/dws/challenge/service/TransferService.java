package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.repository.AccountsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class TransferService {

    private final AccountsRepository accountsRepository;
    private final NotificationService notificationService;
    private final Lock lock = new ReentrantLock();

    @Autowired
    public TransferService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsRepository = accountsRepository;
        this.notificationService = notificationService;
    }

    /**
     * Transfers a specified amount between two accounts.
     *
     * @param accountFromId ID of the source account.
     * @param accountToId   ID of the destination account.
     * @param amount        Amount to transfer.
     * @throws IllegalArgumentException If accounts are invalid or insufficient balance.
     */
    public void transfer(String accountFromId, String accountToId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive.");
        }

        if (accountFromId.equals(accountToId)) {
            throw new IllegalArgumentException("Cannot transfer money to the same account.");
        }

        lock.lock();
        try {
            Account accountFrom = accountsRepository.getAccount(accountFromId);
            Account accountTo = accountsRepository.getAccount(accountToId);

            if (accountFrom == null || accountTo == null) {
                throw new IllegalArgumentException("One or both accounts do not exist.");
            }

            if (accountFrom.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient balance in the source account.");
            }

            accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
            accountTo.setBalance(accountTo.getBalance().add(amount));

            notificationService.notifyAboutTransfer(accountFrom, String.format("Transferred %.2f to account ID: %s", amount, accountToId));
            notificationService.notifyAboutTransfer(accountTo, String.format("Received %.2f from account ID: %s", amount, accountFromId));
        } finally {
            lock.unlock();
        }
    }
}