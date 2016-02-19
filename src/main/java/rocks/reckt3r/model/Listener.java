package rocks.reckt3r.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by hannes on 19.02.16.
 */
@Entity
public class Listener {

    @Id
    @GeneratedValue
    long id;

    @OneToOne
    User user;

    String name;

    @Column(unique=true, nullable=false)
    String token;

    Date lastCalled;

    Date lastWarned;

    Status status;

    long secondsBetweenChecks;

    @Column(length = 500)
    String lastMessage;


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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getLastCalled() {
        return lastCalled;
    }

    public void setLastCalled(Date lastCalled) {
        this.lastCalled = lastCalled;
    }

    public long getSecondsBetweenChecks() {
        return secondsBetweenChecks;
    }

    public void setSecondsBetweenChecks(long secondsBetweenChecks) {
        this.secondsBetweenChecks = secondsBetweenChecks;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        if(lastMessage.length() > 500) {
            lastMessage = lastMessage.substring(0, 497) + "...";
        }
        this.lastMessage = lastMessage;
    }

    public Date getLastWarned() {
        return lastWarned;
    }

    public void setLastWarned(Date lastWarned) {
        this.lastWarned = lastWarned;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
