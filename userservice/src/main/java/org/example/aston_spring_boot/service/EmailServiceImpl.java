package org.example.aston_spring_boot.service;

import org.example.aston_spring_boot.exception.UserNotFoundException;
import org.example.aston_spring_boot.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class EmailServiceImpl implements EmailService{
    private final UserRepository userRepository;

    @Override
    public void send(String email, String message) {

        if(!userRepository.existsByEmail(email)){
            throw new UserNotFoundException(String.format("User with email %s not found", email));
        }


        log.info("Message to {} successfully sent", email);
    }
}
