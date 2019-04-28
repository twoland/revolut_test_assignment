package app.controller;

import app.model.Balance;
import io.javalin.Handler;
import org.eclipse.jetty.http.HttpStatus;

import java.util.List;

import static app.App.balanceDAO;

public class BalanceController {

    public static Handler getBalance = ctx -> {
        Balance balance = balanceDAO.getByAccountIdAndCurrencyCode(ctx.pathParam("id"), ctx.pathParam("currency"));

        if (balance == null) {
            ctx.status(HttpStatus.NOT_FOUND_404);
            return;
        }

        ctx.json(balance);
    };

    public static Handler getAllBalances = ctx -> {
        List<Balance> balances = balanceDAO.getByAccountId(ctx.pathParam("id"));

        ctx.json(balances);
    };

}
