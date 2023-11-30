package antifraud.models;

import lombok.Data;

@Data
public class UserDeletedMessage {
    String username, status;

    public UserDeletedMessage(String username, String status) {
        this.username = username;
        this.status = status;
    }
}
