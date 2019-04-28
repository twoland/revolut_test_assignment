package app.dao;

import app.model.Balance;

import java.util.List;

import static app.App.balanceRepository;

public class BalanceDAO {

    public List<Balance> getByAccountId(String accountId) {
        return balanceRepository.getAll(accountId);
    }

    public Balance getByAccountIdAndCurrencyCode(String accountId, String currencyCode) {
        return balanceRepository.get(accountId, currencyCode.toUpperCase());
    }

}
