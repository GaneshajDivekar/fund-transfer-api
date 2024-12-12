package com.dws.challenge.web;

import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.service.TransferService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/transfer")
public class TransferController {

    private final TransferService transferService;

    @Autowired
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    /**
     * Handles money transfer between accounts.
     *
     * @param accountFromId ID of the source account.
     * @param accountToId   ID of the destination account.
     * @param amount        Amount to be transferred.
     * @return Response indicating success or failure of the transfer.
     */
    @PostMapping
    public ResponseEntity<String> transferMoney(
            @RequestParam @NotNull String accountFromId,
            @RequestParam @NotNull String accountToId,
            @RequestParam @NotNull BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Transfer amount must be positive.");
        }
        if (accountFromId.equals(accountToId)) {
            return ResponseEntity.badRequest().body("Source and destination accounts must be different.");
        }
        try {
            transferService.transfer(accountFromId, accountToId, amount);
            return ResponseEntity.ok("Transfer successful.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Validation failed: " + e.getMessage());
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient funds: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during money transfer: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
}
