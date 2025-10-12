package org.example.task3_1.service;

import org.example.task3_1.dao.UserDAO;
import org.example.task3_1.dao.UserDAOImpl;
import org.example.task3_1.model.User;

import java.util.List;
import java.util.Objects;

public class UserService {
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAOImpl();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = Objects.requireNonNull(userDAO, "userDAO must not be null");
    }

    public User findUser(Long id){
        return userDAO.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("User with id %d not found", id)));
    }

    public User createUser(User user){
        return userDAO.create(user);
    }

    public void deleteUser(Long id){
        userDAO.delete(id);
    }

    public User updateUser(User user){
        return userDAO.update(user);
    }

    public List<User> findAllUsers(){
        return userDAO.findAll();
    }
}