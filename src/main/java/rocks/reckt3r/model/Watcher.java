package rocks.reckt3r.model;

import org.springframework.http.HttpStatus;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.net.URL;
import java.util.Date;

/**
 * Created by hannes on 18.02.16.
 */
@Entity
public class Watcher {

    @Id
    @GeneratedValue
    long id;

    String name;

    @OneToOne
    User user;

    URL urlToWatch;

    Date lastChecked;

    long secondsBetweenChecks;

    HttpStatus expectedStatus;

    Date lastSuccessAt;

    String lastMessage;

    WatcherStatus status;


    public WatcherStatus getStatus() {
        return status;
    }

    public void setStatus(WatcherStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Date getLastSuccessAt() {
        return lastSuccessAt;
    }

    public void setLastSuccessAt(Date lastSuccessAt) {
        this.lastSuccessAt = lastSuccessAt;
    }

    public HttpStatus getExpectedStatus() {
        return expectedStatus;
    }

    public void setExpectedStatus(HttpStatus expectedStatus) {
        this.expectedStatus = expectedStatus;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(Date lastChecked) {
        this.lastChecked = lastChecked;
    }

    public URL getUrlToWatch() {
        return urlToWatch;
    }

    public void setUrlToWatch(URL urlToWatch) {
        this.urlToWatch = urlToWatch;
    }

    public long getSecondsBetweenChecks() {
        return secondsBetweenChecks;
    }

    public void setSecondsBetweenChecks(long secondsBetweenChecks) {
        this.secondsBetweenChecks = secondsBetweenChecks;
    }
}
