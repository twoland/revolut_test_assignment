package app.controller;

import app.model.Account;
import io.javalin.Handler;
import org.eclipse.jetty.http.HttpStatus;

import static app.App.accountDao;

public class AccountController {

    public static Handler getAccount = ctx -> {
        Account account = accountDao.getById(ctx.pathParam("id"));

        if (account == null) {
            ctx.status(HttpStatus.NOT_FOUND_404);
            return;
        }

        ctx.json(account);
    };

}
