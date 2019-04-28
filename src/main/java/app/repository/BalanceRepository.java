package app.repository;

import app.model.Balance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BalanceRepository {

    private static final List<Balance> DATASTORE = new ArrayList<>();

    public List<Balance> getAll(String accountId) {
        return DATASTORE.stream().filter(balance -> (balance.getAccountId().equals(accountId))).collect(Collectors.toList());
    }

    public Balance get(String accountId, String currencyCode) {
        return DATASTORE.stream().filter(balance -> (balance.getAccountId().equals(accountId) && balance.getCurrency().getCurrencyCode().equals(currencyCode))).findFirst().orElse(null);
    }

    public void add(Balance balance) {
        DATASTORE.add(balance);
    }

    public void reset() {
        DATASTORE.clear();
    }
}
