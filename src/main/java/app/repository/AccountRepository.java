package app.repository;

import app.model.Account;

import java.util.ArrayList;
import java.util.List;

public class AccountRepository {

    private static final List<Account> DATASTORE = new ArrayList<>();

    public Account get(String id) {
        return DATASTORE.stream().filter(account -> account.getId().equals(id)).findFirst().orElse(null);
    }

    public void add(Account account) {
        DATASTORE.add(account);
    }

    public void reset() {
        DATASTORE.clear();
    }
}
