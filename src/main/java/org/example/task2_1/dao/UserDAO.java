package org.example.task2_1.dao;

import org.example.task2_1.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    User create(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    User update(User user);
    boolean delete(Long id);
}