package rocks.reckt3r.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import rocks.reckt3r.model.User;
import rocks.reckt3r.model.Watcher;

import java.util.List;

/**
 * Created by hannes on 18.02.16.
 */
public interface WatcherRepository extends JpaRepository<Watcher, Long> {

    List<Watcher> findAllByUser(@Param("user") User user);

    Watcher findOneByUserAndName(@Param("user") User user, @Param("name") String name);
}
