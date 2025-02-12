package org.xiyu.yee.capespringboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xiyu.yee.capespringboot.model.User;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findAll();
    void delete(User user);
    User save(User user);
    List<User> findByDeleteAtLessThanEqual(LocalDateTime dateTime);
} 