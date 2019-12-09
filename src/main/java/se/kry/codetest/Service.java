package se.kry.codetest;


import java.util.Date;

public class Service {
    private String name;
    private String url;
    private String added_by;
    private Date created_at;
    private String last_status;

    public Service(String name, String url, String added_by) {
        this(name, url, added_by, new Date(), "UNKNOWN");
    }

    public Service(String name, String url, String added_by, Date created_at, String last_status) {
        this.name = name;
        this.url = url;
        this.added_by = added_by;
        this.created_at = created_at;
        this.last_status = last_status;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getAddedBy() {
        return added_by;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public String getStatus() {
        return last_status;
    }

    public void setStatus(String status) {
        this.last_status = status;
    }
}
