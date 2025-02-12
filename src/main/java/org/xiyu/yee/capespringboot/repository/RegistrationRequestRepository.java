package org.xiyu.yee.capespringboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.xiyu.yee.capespringboot.model.RegistrationRequest;
import java.util.List;

@Repository
public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {
    List<RegistrationRequest> findByStatus(String status);
    
    long countByStatus(String status);
} 