package antifraud.repositories;

import antifraud.models.StolenCard;
import antifraud.models.Transaction;
import antifraud.models.TransactionLimits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionLimitsRepository extends JpaRepository<TransactionLimits, Long> {
    Optional<TransactionLimits> findFirstByOrderByIdDesc();

    TransactionLimits save(TransactionLimits transactionLimits);

    void delete(TransactionLimits transactionLimits);
}
