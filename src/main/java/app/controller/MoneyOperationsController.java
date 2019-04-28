package app.controller;

import app.model.Account;
import app.model.Balance;
import app.model.Transaction;
import app.model.util.TransactionStatus;
import io.javalin.Handler;
import org.eclipse.jetty.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Currency;

import static app.App.*;

public class MoneyOperationsController {

    public static Handler makeMoneyTransfer = ctx -> {
        // Checking if the sender account exists
        Account sender = accountDao.getById(ctx.pathParam("id"));
        if (sender == null) {
            ctx.status(HttpStatus.NOT_FOUND_404);
            return;
        }

        // Checking if the receiver account exists
        Account receiver = accountDao.getById(ctx.formParam("receiver"));
        if (receiver == null) {
            ctx.status(HttpStatus.NOT_FOUND_404);
            return;
        }

        // Checking if the value has been passed
        String valueString = ctx.formParam("value");
        if (valueString == null) {
            ctx.status(HttpStatus.FORBIDDEN_403);
            ctx.result("Missing param: value");
            return;
        }

        // Checking if the value is positive
        BigDecimal value = new BigDecimal(valueString);
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            ctx.status(HttpStatus.FORBIDDEN_403);
            ctx.result("Incorrect param: value | Reason: has to be positive");
            return;
        }

        // Getting the currency code
        String currencyCode = ctx.pathParam("currency");

        // Checking balance
        Balance balance = balanceDAO.getByAccountIdAndCurrencyCode(sender.getId(), currencyCode);
        if ((balance == null) || (value.compareTo(balance.getValue()) > 0)) {
            ctx.status(HttpStatus.FORBIDDEN_403);
            ctx.result("Insufficient balance");
            return;
        }

        // ------------------------ Everything's valid, proceeding with the transaction ------------------------

        // Creating the transaction
        Transaction transaction = new Transaction(sender.getId(), receiver.getId(), currencyCode, value);
        transactionRepository.add(transaction);

        // Adding transaction status to the output BEFORE transaction execution
        // The status of the transaction will be PENDING like it should in real async environment
        // The client should check the transaction status afterward
        ctx.json(transaction);

        // Executing the transaction
        executeTransaction(transaction);
    };

    public static void executeTransaction(Transaction transaction) {
        Balance receiverBalance = balanceDAO.getByAccountIdAndCurrencyCode(transaction.getReceiverId(), transaction.getCurrency());

        // Creating receiver's balance if doesn't exist
        if (receiverBalance == null) {
            receiverBalance = new Balance(transaction.getReceiverId(), Currency.getInstance(transaction.getCurrency()), BigDecimal.valueOf(0));
            balanceRepository.add(receiverBalance);
        }

        // Checking and executing the transaction
        if (checkTransaction(transaction)) {
            // Checking sender balance (again)
            Balance senderBalance = balanceDAO.getByAccountIdAndCurrencyCode(transaction.getSenderId(), transaction.getCurrency());
            if ((senderBalance == null) || (transaction.getValue().compareTo(senderBalance.getValue()) > 0)) {
                transaction.setStatus(TransactionStatus.DENIED);
                return;
            }

            // Moving the funds
            senderBalance.add(transaction.getValue().negate());
            receiverBalance.add(transaction.getValue());
            transaction.setStatus(TransactionStatus.SUCCESSFUL);
        }
        else {
            denyTransaction(transaction);
        }
    }

    // Mock functions for external transaction check

    private static Boolean checkTransaction(Transaction transaction) {
        // This is a mock transaction check
        // This logic should include external requests to fraud control and other services
        // If the transaction fails this check, the status should become DENIED
        return true;
    }

    private static void denyTransaction(Transaction transaction) {
         transaction.setStatus(TransactionStatus.DENIED);
    }

}
