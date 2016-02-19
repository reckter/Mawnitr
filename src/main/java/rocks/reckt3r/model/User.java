package rocks.reckt3r.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by hannes on 18.02.16.
 */
@Entity
public class User {

    @Id
    private long telegramId;

    public long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(long telegramId) {
        this.telegramId = telegramId;
    }
}
