package antifraud.repositories;

import antifraud.models.StolenCard;
import antifraud.models.SuspiciousIP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StolenCardRepository extends JpaRepository<StolenCard, Long> {
    List<StolenCard> findAll();
    List<StolenCard> findAllByOrderByIdAsc();
    Optional<StolenCard> findByNumber(String number);

    StolenCard save(StolenCard stolenCard);

    void delete(StolenCard stolenCard);
}
