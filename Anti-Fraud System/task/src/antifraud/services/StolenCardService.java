package antifraud.services;

import antifraud.models.StolenCard;
import antifraud.repositories.StolenCardRepository;
import antifraud.repositories.SuspiciousIPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StolenCardService {
    private final StolenCardRepository stolenCardRepository;

    @Autowired
    public StolenCardService(StolenCardRepository stolenCardRepository) {
        this.stolenCardRepository = stolenCardRepository;
    }
    public List<StolenCard> findAll() {
        return stolenCardRepository.findAll();
    }
    public List<StolenCard> findAllByOrderByIdAsc() {
        return stolenCardRepository.findAllByOrderByIdAsc();
    }
    public Optional<StolenCard> findByNumber(String number) {
        return stolenCardRepository.findByNumber(number);
    }

    public StolenCard save(StolenCard stolenCard) {
        return stolenCardRepository.save(stolenCard);
    }

    public void delete(StolenCard stolenCard) {
        stolenCardRepository.delete(stolenCard);
    }
}
