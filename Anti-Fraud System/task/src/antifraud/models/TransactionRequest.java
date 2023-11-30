package antifraud.models;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
@Data
public class TransactionRequest {
    long amount;
    @NotEmpty
    String ip, number;
}
