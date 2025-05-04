package com.example.Altaska.repositories;

import com.example.Altaska.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    Optional<Users> findByOldEmailChangeToken(String token);

    Optional<Users> findByNewEmailChangeToken(String token);
}
