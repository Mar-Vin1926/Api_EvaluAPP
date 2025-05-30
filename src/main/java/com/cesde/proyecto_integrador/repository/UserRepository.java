package com.cesde.proyecto_integrador.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cesde.proyecto_integrador.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    @org.springframework.lang.NonNull
    Optional<User> findById(@org.springframework.lang.NonNull Long id);
    User findByEmail(String email);
    List<User> findByRole(User.Role role);
}
