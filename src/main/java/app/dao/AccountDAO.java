package app.dao;

import app.model.Account;

import static app.App.accountRepository;

public class AccountDAO {

    public Account getById(String id) {
        return accountRepository.get(id);
    }

}
