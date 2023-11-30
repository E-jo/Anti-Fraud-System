package antifraud.models;

import lombok.Data;

@Data
public class RegistrationRequest {
    String name, userName, password;
}
