package app.dao;

import app.model.Transaction;

import static app.App.transactionRepository;

public class TransactionDAO {

    public Transaction getById(String id) {
        return transactionRepository.get(id);
    }

}
