package antifraud.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Entity
@Table(name = "transaction")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long transactionId;

    @Column(name = "amount")
    private long amount;

    @Column(name = "ip")
    @NotEmpty
    private String ip;

    @Column(name = "number")
    @NotEmpty
    private String number;

    @Column(name = "region")
    @NotEmpty
    private String region;

    @Column(name = "date")
    private String date;

    @Column(name = "result")
    private String result;

    @Column(name = "feedback")
    private String feedback = "";
}
