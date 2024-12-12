package com.dws.challenge;

import com.dws.challenge.exception.AccountNotFoundException;
import com.dws.challenge.exception.InsufficientFundsException;
import com.dws.challenge.service.TransferService;
import com.dws.challenge.web.TransferController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TransferControllerTest {

    private TransferService transferService;
    private TransferController controller;

    @BeforeEach
    void setUp() {
        transferService = mock(TransferService.class);
        controller = new TransferController(transferService);
    }

    @Test
    void testTransferMoney_Success() {
        String accountFrom = "123";
        String accountTo = "456";
        BigDecimal amount = new BigDecimal("100.00");
        doNothing().when(transferService).transfer(accountFrom, accountTo, amount);

        ResponseEntity<String> response = controller.transferMoney(accountFrom, accountTo, amount);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Transfer successful.", response.getBody());
    }

    @Test
    void testTransferMoney_NegativeAmount() {
        String accountFrom = "123";
        String accountTo = "456";
        BigDecimal amount = new BigDecimal("-50.00");

        ResponseEntity<String> response = controller.transferMoney(accountFrom, accountTo, amount);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Transfer amount must be positive.", response.getBody());
    }

    @Test
    void testTransferMoney_SameAccount() {
        String accountFrom = "123";
        String accountTo = "123";
        BigDecimal amount = new BigDecimal("100.00");

        ResponseEntity<String> response = controller.transferMoney(accountFrom, accountTo, amount);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Source and destination accounts must be different.", response.getBody());
    }

    @Test
    void testTransferMoney_AccountNotFound() {
        String accountFrom = "123";
        String accountTo = "999";
        BigDecimal amount = new BigDecimal("100.00");
        doThrow(new AccountNotFoundException("Account not found"))
                .when(transferService).transfer(accountFrom, accountTo, amount);

        ResponseEntity<String> response = controller.transferMoney(accountFrom, accountTo, amount);

        assertEquals(404, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Account not found"));
    }

    @Test
    void testTransferMoney_InsufficientFunds() {
        String accountFrom = "123";
        String accountTo = "456";
        BigDecimal amount = new BigDecimal("1000.00");
        doThrow(new InsufficientFundsException("Insufficient funds"))
                .when(transferService).transfer(accountFrom, accountTo, amount);

        ResponseEntity<String> response = controller.transferMoney(accountFrom, accountTo, amount);


        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Insufficient funds"));
    }

    @Test
    void testTransferMoney_UnexpectedError() {
        String accountFrom = "123";
        String accountTo = "456";
        BigDecimal amount = new BigDecimal("100.00");
        doThrow(new RuntimeException("Unexpected error"))
                .when(transferService).transfer(accountFrom, accountTo, amount);

        ResponseEntity<String> response = controller.transferMoney(accountFrom, accountTo, amount);

        assertEquals(500, response.getStatusCodeValue());
        assertEquals("An unexpected error occurred.", response.getBody());
    }
}
