package antifraud.models;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LockChangeRequest {
    @NotEmpty
    private String username;

    private String operation;
}
