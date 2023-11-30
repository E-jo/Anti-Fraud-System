package antifraud.repositories;

import antifraud.models.StolenCard;
import antifraud.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAll();
    List<Transaction> findAllByOrderByTransactionIdAsc();
    Optional<Transaction> findByNumber(String number);

    Optional<List<Transaction>> findAllByNumber(String number);

    Optional<Transaction> findByTransactionId(long transactionId);

    @Query(value = "SELECT COUNT(DISTINCT region) = 2 " +
            "FROM transaction " +
            "WHERE PARSEDATETIME(date, 'yyyy-MM-dd''T''HH:mm:ss') BETWEEN :startTime AND :endTime " +
            "AND region != :region", nativeQuery = true)
    boolean checkForTwoFromDifferentRegion(@Param("region") String region,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
    @Query(value = "SELECT COUNT(DISTINCT region) > 2 " +
            "FROM transaction " +
            "WHERE PARSEDATETIME(date, 'yyyy-MM-dd''T''HH:mm:ss') BETWEEN :startTime AND :endTime " +
            "AND region != :region", nativeQuery = true)
    boolean checkForMoreThanTwoFromDifferentRegion(@Param("region") String region,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);
    @Query(value = "SELECT COUNT(DISTINCT ip) = 2 " +
            "FROM transaction " +
            "WHERE PARSEDATETIME(date, 'yyyy-MM-dd''T''HH:mm:ss') BETWEEN :startTime AND :endTime " +
            "AND ip != :ip", nativeQuery = true)
    boolean checkForTwoFromDifferentIP(@Param("ip") String ip,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);
    @Query(value = "SELECT COUNT(DISTINCT ip) > 2 " +
            "FROM transaction " +
            "WHERE PARSEDATETIME(date, 'yyyy-MM-dd''T''HH:mm:ss') BETWEEN :startTime AND :endTime " +
            "AND ip != :ip", nativeQuery = true)
    boolean checkForMoreThanTwoFromDifferentIP(@Param("ip") String ip,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    Transaction save(Transaction transaction);

    void delete(Transaction transaction);
}
