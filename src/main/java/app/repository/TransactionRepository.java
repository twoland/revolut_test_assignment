package app.repository;

import app.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {

    private static final List<Transaction> DATASTORE = new ArrayList<>();

    public Transaction get(String id) {
        return DATASTORE.stream().filter(transaction -> transaction.getId().equals(id)).findFirst().orElse(null);
    }

    public String add(Transaction transaction) {
        // Imitating a unique ID creation
        // Let's assume that we won't get 2 transactions in the same millisecond
        transaction.setId(transaction.getSenderId() + transaction.getReceiverId() + System.currentTimeMillis());
        DATASTORE.add(transaction);

        return transaction.getId();
    }

    public void reset() {
        DATASTORE.clear();
    }
}
