package app.model;

public class Account {
    private String id;
    private String meta;

    public Account(String id, String meta) {
        this.id = id;
        this.meta = meta;
    }

    public String getId() {
        return id;
    }

    public String getMeta() {
        return meta;
    }
}

