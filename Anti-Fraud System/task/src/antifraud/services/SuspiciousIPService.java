package antifraud.services;

import antifraud.models.SuspiciousIP;
import antifraud.repositories.SuspiciousIPRepository;
import antifraud.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SuspiciousIPService {
    private final SuspiciousIPRepository suspiciousIPRepository;

    @Autowired
    public SuspiciousIPService(SuspiciousIPRepository suspiciousIPRepository) {
        this.suspiciousIPRepository = suspiciousIPRepository;
    }

    public Optional<SuspiciousIP> findByIp(String ip) {
        return suspiciousIPRepository.findByIp(ip);
    }

    public List<SuspiciousIP> findAll() {
        return suspiciousIPRepository.findAll();
    }
    public List<SuspiciousIP> findAllByOrderByIdAsc() {
        return suspiciousIPRepository.findAllByOrderByIdAsc();
    }

    public SuspiciousIP save(SuspiciousIP suspiciousIP) {
        return suspiciousIPRepository.save(suspiciousIP);
    }

    public void delete(SuspiciousIP suspiciousIP) {
        suspiciousIPRepository.delete(suspiciousIP);
    }
}
