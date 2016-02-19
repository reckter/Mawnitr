package rocks.reckt3r.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import rocks.reckt3r.model.Listener;
import rocks.reckt3r.model.User;
import rocks.reckt3r.model.Watcher;

import java.util.List;

/**
 * Created by hannes on 19.02.16.
 */
public interface ListenerRepository extends JpaRepository<Listener, Long> {

    List<Listener> findAllByUser(@Param("user") User user);

    Listener findOneByUserAndName(@Param("user") User user, @Param("name") String name);

    Listener findByToken(@Param("token") String token);
}
