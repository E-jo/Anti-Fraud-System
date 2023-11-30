package antifraud.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class TransactionLimits {
    @Id
    @GeneratedValue
    long id;

    private int allowedLimit, manualLimit;

    public TransactionLimits(int allowedLimit, int manualLimit) {
        this.allowedLimit = allowedLimit;
        this.manualLimit = manualLimit;
    }
}
