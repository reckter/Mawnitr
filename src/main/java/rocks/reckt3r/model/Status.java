package rocks.reckt3r.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

/**
 * Created by hannes on 18.02.16.
 */
public enum Status {
    ONLINE("online"),
    OFFLINE("offline");



    String value;
    Status(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static Status fromValue(String value) {
        for(Status at : Status.values()) {
            if(Objects.equals(at.value(), value)) {
                return at;
            }
        }

        throw new IllegalArgumentException("Can't deserialize Status with value '" + value + "'");
    }
}
