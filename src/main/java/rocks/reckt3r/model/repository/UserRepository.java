package rocks.reckt3r.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rocks.reckt3r.model.User;

/**
 * Created by hannes on 18.02.16.
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
