package app;

import app.controller.AccountController;
import app.controller.BalanceController;
import app.controller.MoneyOperationsController;
import app.controller.TransactionController;
import app.dao.AccountDAO;
import app.dao.BalanceDAO;
import app.dao.TransactionDAO;
import app.repository.AccountRepository;
import app.repository.BalanceRepository;
import app.repository.TransactionRepository;
import io.javalin.Javalin;

import static io.javalin.apibuilder.ApiBuilder.*;

public class App {

    private static Javalin app;

    public static AccountRepository accountRepository;
    public static BalanceRepository balanceRepository;
    public static TransactionRepository transactionRepository;

    public static AccountDAO accountDao;
    public static BalanceDAO balanceDAO;
    public static TransactionDAO transactionDAO;

    public static Javalin init(Integer port) {
        stop();
        initRepositories();
        initDAO();

        app = Javalin.create().start(port);

        app.routes(() -> {
            path("api", () -> {
                // Account info and actions
                path("account", () -> {
                    path(":id", () -> {
                        get(AccountController.getAccount);

                        path("balance", () -> {
                            get(BalanceController.getAllBalances);

                            path(":currency", () -> {
                                get(BalanceController.getBalance);
                            });
                        });

                        // Since there's no auth and no session we'll be sending money from this path
                        path("send", () -> {
                            path(":currency", () -> {
                                post(MoneyOperationsController.makeMoneyTransfer);
                            });
                        });
                    });
                });

                // Transaction status check
                path("transaction", () -> {
                    path(":id", () -> {
                        get(TransactionController.getTransaction);
                    });
                });
            });
        });

        return app;
    }

    private static void initRepositories() {
        accountRepository = new AccountRepository();
        balanceRepository = new BalanceRepository();
        transactionRepository = new TransactionRepository();
    }

    private static void initDAO() {
        accountDao = new AccountDAO();
        balanceDAO = new BalanceDAO();
        transactionDAO = new TransactionDAO();
    }

    public static void stop() {
        if (app != null) {
            app.stop();
        }
    }
}
