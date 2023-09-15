package com.mysite.sbb.Repository;

import com.mysite.sbb.entity.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<SiteUser, Long> {
    

    Optional<SiteUser> findByusername(String username);

    Optional<SiteUser> findByUsername(String username);
}
