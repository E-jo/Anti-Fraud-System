package antifraud.repositories;

import antifraud.models.User;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAll();
    List<User> findAllByOrderByIdAsc();
    Optional<User> findByUsername(String email);
    Optional<User> findByUsernameIgnoreCase(String email);

    User save(User user);

    void delete(User user);
}
