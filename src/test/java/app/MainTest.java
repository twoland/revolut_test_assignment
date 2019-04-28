package app;

import app.model.Account;
import app.model.Balance;
import app.util.RequestUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.Currency;
import java.util.HashMap;

import static app.App.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MainTest {

    public static final int PORT = 8008;
    public static final String SERVER_URL = "http://localhost:" + 8008 + "/";
    public static final String API_URL = SERVER_URL + "api/";

    // Accounts
    public static Account account1;
    public static Account account2;
    public static Account account3;

    @BeforeAll
    static void initAll() {
        App.init(PORT);
    }

    @BeforeEach
    void init() {
        // Creating accounts
        account1 = new Account("1234567890", "Rich Guy");
        account2 = new Account("1111111111", "Friend from London");
        account3 = new Account("2222222222", "Friend from New York");
        accountRepository.add(account1);
        accountRepository.add(account2);
        accountRepository.add(account3);

        // Creating balances
        // Rich guy
        balanceRepository.add(new Balance(account1.getId(), Currency.getInstance("USD"), BigDecimal.valueOf(1000)));
        balanceRepository.add(new Balance(account1.getId(), Currency.getInstance("EUR"), BigDecimal.valueOf(1000)));
        balanceRepository.add(new Balance(account1.getId(), Currency.getInstance("GBP"), BigDecimal.valueOf(1000)));

        // London
        balanceRepository.add(new Balance(account2.getId(), Currency.getInstance("GBP"), BigDecimal.valueOf(100)));

        // New York
        balanceRepository.add(new Balance(account3.getId(), Currency.getInstance("USD"), BigDecimal.valueOf(200)));
    }

    @Test
    void accountExistsTest() {
        try {
            HttpURLConnection connection = RequestUtils.prepareRequest(API_URL + "account/" + account1.getId(), "GET");
            assertEquals(HttpStatus.OK_200, connection.getResponseCode());

            String response = RequestUtils.readResponse(connection);
            connection.disconnect();

            if (response.equals("")) {
                fail("Empty response");
            }

            JSONObject responseJSON = new JSONObject(response);
            assertEquals(account1.getId(), responseJSON.getString("id"));
        }
        catch (IOException exception) {
            fail("Request IO exception: " + exception.getMessage());
        }
        catch (Exception exception) {
            fail("Test run exception: " + exception.getMessage());
        }
    }

    @Test
    void account404Test() {
        try {
            HttpURLConnection connection = RequestUtils.prepareRequest(API_URL + "account/000", "GET");
            assertEquals(HttpStatus.NOT_FOUND_404, connection.getResponseCode());
            connection.disconnect();
        }
        catch (IOException exception) {
            fail("Request IO exception: " + exception.getMessage());
        }
        catch (Exception exception) {
            fail("Test run exception: " + exception.getMessage());
        }
    }

    @Test
    void balanceAllTest() {
        try {
            HttpURLConnection connection = RequestUtils.prepareRequest(API_URL + "account/" + account1.getId() + "/balance/", "GET");
            assertEquals(HttpStatus.OK_200, connection.getResponseCode());

            String response = RequestUtils.readResponse(connection);
            connection.disconnect();

            if (response.equals("")) {
                fail("Empty response");
            }

            JSONArray responseJSON = new JSONArray(response);

            // Checking 3 balances
            JSONObject balance1 = responseJSON.getJSONObject(0);
            JSONObject balance2 = responseJSON.getJSONObject(1);
            JSONObject balance3 = responseJSON.getJSONObject(2);

            assertEquals(account1.getId(), balance1.getString("accountId"));
            assertEquals(account1.getId(), balance2.getString("accountId"));
            assertEquals(account1.getId(), balance3.getString("accountId"));

            assertEquals("USD", balance1.getString("currency"));
            assertEquals("EUR", balance2.getString("currency"));
            assertEquals("GBP", balance3.getString("currency"));

            assertEquals(1000, balance1.getInt("value"));
            assertEquals(1000, balance2.getInt("value"));
            assertEquals(1000, balance3.getInt("value"));
        }
        catch (IOException exception) {
            fail("Request IO exception: " + exception.getMessage());
        }
        catch (Exception exception) {
            fail("Test run exception: " + exception.getMessage());
        }
    }

    @Test
    void balanceForCurrencyTest() {
        try {
            HttpURLConnection connection = RequestUtils.prepareRequest(API_URL + "account/" + account1.getId() + "/balance/GBP", "GET");
            assertEquals(HttpStatus.OK_200, connection.getResponseCode());

            String response = RequestUtils.readResponse(connection);
            connection.disconnect();

            if (response.equals("")) {
                fail("Empty response");
            }

            JSONObject responseJSON = new JSONObject(response);
            assertEquals(account1.getId(), responseJSON.getString("accountId"));
            assertEquals("GBP", responseJSON.getString("currency"));
            assertEquals(1000, responseJSON.getInt("value"));
        }
        catch (IOException exception) {
            fail("Request IO exception: " + exception.getMessage());
        }
        catch (Exception exception) {
            fail("Test run exception: " + exception.getMessage());
        }
    }

    @Test
    void balance404Test() {
        try {
            HttpURLConnection connection = RequestUtils.prepareRequest(API_URL + "account/" + account1.getId() + "/balance/JPY", "GET");
            assertEquals(HttpStatus.NOT_FOUND_404, connection.getResponseCode());
            connection.disconnect();
        }
        catch (IOException exception) {
            fail("Request IO exception: " + exception.getMessage());
        }
        catch (Exception exception) {
            fail("Test run exception: " + exception.getMessage());
        }
    }

    @Test
    void moneyTransferSuccess1Test() {
        try {
            Integer sendValue = 100;

            HashMap<String, String> params = new HashMap<>();
            params.put("receiver", account2.getId());
            params.put("value", sendValue.toString());

            HttpURLConnection connection = RequestUtils.prepareRequest(API_URL + "account/" + account1.getId() + "/send/GBP", "POST", params);
            assertEquals(HttpStatus.OK_200, connection.getResponseCode());

            String response = RequestUtils.readResponse(connection);
            connection.disconnect();

            if (response.equals("")) {
                fail("Empty response");
            }

            JSONObject responseJSON = new JSONObject(response);
            String transactionId = responseJSON.getString("id");

            assertEquals("PENDING", responseJSON.getString("status"));
            assertEquals("GBP", responseJSON.getString("currency"));
            assertEquals(sendValue, responseJSON.getInt("value"));

            connection = RequestUtils.prepareRequest(API_URL + "transaction/" + transactionId, "GET");
            assertEquals(HttpStatus.OK_200, connection.getResponseCode());
            response = RequestUtils.readResponse(connection);
            connection.disconnect();

            responseJSON = new JSONObject(response);
            assertEquals("SUCCESSFUL", responseJSON.getString("status"));

            // -100 GBP on Rich Guy's balance
            Balance account1GBP = balanceDAO.getByAccountIdAndCurrencyCode(account1.getId(), "GBP");
            assertEquals(account1GBP.getValue().compareTo(BigDecimal.valueOf(900)), 0);

            // +100 GBP on London friend's existing balance
            Balance account2GBP = balanceDAO.getByAccountIdAndCurrencyCode(account2.getId(), "GBP");
            assertEquals(account2GBP.getValue().compareTo(BigDecimal.valueOf(200)), 0);
        }
        catch (IOException exception) {
            fail("Request IO exception: " + exception.getMessage());
        }
        catch (Exception exception) {
            fail("Test run exception: " + exception.getMessage());
        }
    }

    @Test
    void moneyTransferSuccess2Test() {
        try {
            Integer sendValue = 100;

            HashMap<String, String> params = new HashMap<>();
            params.put("receiver", account3.getId());
            params.put("value", sendValue.toString());

            HttpURLConnection connection = RequestUtils.prepareRequest(API_URL + "account/" + account1.getId() + "/send/GBP", "POST", params);
            assertEquals(HttpStatus.OK_200, connection.getResponseCode());

            String response = RequestUtils.readResponse(connection);
            connection.disconnect();

            if (response.equals("")) {
                fail("Empty response");
            }

            JSONObject responseJSON = new JSONObject(response);
            String transactionId = responseJSON.getString("id");

            assertEquals("PENDING", responseJSON.getString("status"));
            assertEquals("GBP", responseJSON.getString("currency"));
            assertEquals(sendValue, responseJSON.getInt("value"));

            connection = RequestUtils.prepareRequest(API_URL + "transaction/" + transactionId, "GET");
            assertEquals(HttpStatus.OK_200, connection.getResponseCode());
            response = RequestUtils.readResponse(connection);
            connection.disconnect();

            responseJSON = new JSONObject(response);
            assertEquals("SUCCESSFUL", responseJSON.getString("status"));

            // -100 GBP on Rich Guy's balance
            Balance account1GBP = balanceDAO.getByAccountIdAndCurrencyCode(account1.getId(), "GBP");
            assertEquals(account1GBP.getValue().compareTo(BigDecimal.valueOf(900)), 0);

            // 100 GBP on NY friend's NEW balance
            Balance account2GBP = balanceDAO.getByAccountIdAndCurrencyCode(account3.getId(), "GBP");
            assertEquals(account2GBP.getValue().compareTo(BigDecimal.valueOf(sendValue)), 0);
        }
        catch (IOException exception) {
            fail("Request IO exception: " + exception.getMessage());
        }
        catch (Exception exception) {
            fail("Test run exception: " + exception.getMessage());
        }
    }

    @Test
    void moneyTransferInsufficientBalanceTest() {
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("receiver", account3.getId());
            params.put("value", "2000");

            HttpURLConnection connection = RequestUtils.prepareRequest(API_URL + "account/" + account1.getId() + "/send/GBP", "POST", params);
            assertEquals(HttpStatus.FORBIDDEN_403, connection.getResponseCode());
            connection.disconnect();
        }
        catch (IOException exception) {
            fail("Request IO exception: " + exception.getMessage());
        }
        catch (Exception exception) {
            fail("Test run exception: " + exception.getMessage());
        }
    }

    @Test
    void moneyTransferNegativeValueTest() {
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("receiver", account3.getId());
            params.put("value", "-100");

            HttpURLConnection connection = RequestUtils.prepareRequest(API_URL + "account/" + account1.getId() + "/send/GBP", "POST", params);
            assertEquals(HttpStatus.FORBIDDEN_403, connection.getResponseCode());
            connection.disconnect();
        }
        catch (IOException exception) {
            fail("Request IO exception: " + exception.getMessage());
        }
        catch (Exception exception) {
            fail("Test run exception: " + exception.getMessage());
        }
    }

    @Test
    void moneyTransferMissingValueTest() {
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("receiver", account3.getId());

            HttpURLConnection connection = RequestUtils.prepareRequest(API_URL + "account/" + account1.getId() + "/send/GBP", "POST", params);
            assertEquals(HttpStatus.FORBIDDEN_403, connection.getResponseCode());
            connection.disconnect();
        }
        catch (IOException exception) {
            fail("Request IO exception: " + exception.getMessage());
        }
        catch (Exception exception) {
            fail("Test run exception: " + exception.getMessage());
        }
    }

    @Test
    void moneyTransferMissingReceiverTest() {
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("value", "100");

            HttpURLConnection connection = RequestUtils.prepareRequest(API_URL + "account/" + account1.getId() + "/send/GBP", "POST", params);
            assertEquals(HttpStatus.NOT_FOUND_404, connection.getResponseCode());
            connection.disconnect();
        }
        catch (IOException exception) {
            fail("Request IO exception: " + exception.getMessage());
        }
        catch (Exception exception) {
            fail("Test run exception: " + exception.getMessage());
        }
    }

    @Test
    void moneyTransferWrongReceiverTest() {
        try {
            HashMap<String, String> params = new HashMap<>();
            params.put("receiver", "abc0101");
            params.put("value", "100");

            HttpURLConnection connection = RequestUtils.prepareRequest(API_URL + "account/" + account1.getId() + "/send/GBP", "POST", params);
            assertEquals(HttpStatus.NOT_FOUND_404, connection.getResponseCode());
            connection.disconnect();
        }
        catch (IOException exception) {
            fail("Request IO exception: " + exception.getMessage());
        }
        catch (Exception exception) {
            fail("Test run exception: " + exception.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        accountRepository.reset();
        balanceRepository.reset();
        transactionRepository.reset();
    }

    @AfterAll
    static void tearDownAll() {
        App.stop();
    }
}
