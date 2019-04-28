package app.model;

import app.model.util.TransactionStatus;

import java.math.BigDecimal;

public class Transaction {
    private String id;
    private String senderId;
    private String receiverId;
    private String currency;
    private BigDecimal value;
    private TransactionStatus status;

    public Transaction(String senderId, String receiverId, String currency, BigDecimal value) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.currency = currency;
        this.value = value;
        this.status = TransactionStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getValue() {
        return value;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public void setId(String id) {
        this.id = id;
    }
}

