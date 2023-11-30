package antifraud.models;

import lombok.Data;

@Data
public class RoleChangeRequest {
    String username, role;
}
