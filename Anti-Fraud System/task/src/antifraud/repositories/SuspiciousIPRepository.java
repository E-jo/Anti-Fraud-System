package antifraud.repositories;

import antifraud.models.SuspiciousIP;
import antifraud.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuspiciousIPRepository extends JpaRepository<SuspiciousIP, Long> {
    List<SuspiciousIP> findAll();
    List<SuspiciousIP> findAllByOrderByIdAsc();
    Optional<SuspiciousIP> findByIp(String ip);

    SuspiciousIP save(SuspiciousIP suspiciousIP);

    void delete(SuspiciousIP suspiciousIP);
}