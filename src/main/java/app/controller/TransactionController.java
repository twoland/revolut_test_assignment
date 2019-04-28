package app.controller;

import app.model.Transaction;
import io.javalin.Handler;
import org.eclipse.jetty.http.HttpStatus;

import static app.App.transactionDAO;

public class TransactionController {

    public static Handler getTransaction = ctx -> {
        Transaction transaction = transactionDAO.getById(ctx.pathParam("id"));

        if (transaction == null) {
            ctx.status(HttpStatus.NOT_FOUND_404);
            return;
        }

        ctx.json(transaction);
    };

}
