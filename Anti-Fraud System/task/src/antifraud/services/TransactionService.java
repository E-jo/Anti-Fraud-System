package antifraud.services;

import antifraud.models.StolenCard;
import antifraud.models.Transaction;
import antifraud.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    public boolean checkForTwoFromDifferentRegion(@Param("region") String region,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime) {
        return transactionRepository.checkForTwoFromDifferentRegion(region, startTime, endTime);
    }
    public boolean checkForMoreThanTwoFromDifferentRegion(@Param("region") String region,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime) {
        return transactionRepository.checkForMoreThanTwoFromDifferentRegion(region, startTime, endTime);
    }

    public boolean checkForTwoFromDifferentIP(@Param("ip") String ip,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime) {
        return transactionRepository.checkForTwoFromDifferentIP(ip, startTime, endTime);
    }
    public boolean checkForMoreThanTwoFromDifferentIP(@Param("ip") String ip,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime) {
        return transactionRepository.checkForMoreThanTwoFromDifferentIP(ip, startTime, endTime);
    }

    public Optional<Transaction> findByTransactionId(long transactionId) {
        return transactionRepository.findByTransactionId(transactionId);
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public Optional<List<Transaction>> findAllByNumber(String number) {
        return transactionRepository.findAllByNumber(number);
    }

    public List<Transaction> findAllByOrderByTransactionIdAsc() {
        return transactionRepository.findAllByOrderByTransactionIdAsc();
    }
    public Optional<Transaction> findByNumber(String number) {
        return transactionRepository.findByNumber(number);
    }

    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public void delete(Transaction transaction) {
        transactionRepository.delete(transaction);
    }
}
