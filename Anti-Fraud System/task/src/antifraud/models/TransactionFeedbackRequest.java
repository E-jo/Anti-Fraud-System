package antifraud.models;

import lombok.Data;

@Data
public class TransactionFeedbackRequest {
    long transactionId;
    String feedback;
}
