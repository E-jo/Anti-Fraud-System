package antifraud;

import antifraud.models.TransactionLimits;
import antifraud.repositories.TransactionLimitsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import org.springframework.boot.ApplicationRunner;

@Component
public class LoadInitialTransactionLimits implements ApplicationRunner {

    private final TransactionLimitsRepository transactionLimitsRepository;

    @Autowired
    public LoadInitialTransactionLimits(TransactionLimitsRepository transactionLimitsRepository) {
        this.transactionLimitsRepository = transactionLimitsRepository;
    }

    public void run(ApplicationArguments args) {
        if (transactionLimitsRepository.findFirstByOrderByIdDesc().isEmpty()) {
            transactionLimitsRepository.save(new TransactionLimits(200, 1500));
        }
    }
}