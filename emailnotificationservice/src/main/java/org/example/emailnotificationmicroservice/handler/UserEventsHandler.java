package org.example.emailnotificationmicroservice.handler;

import org.example.events.UserCreatedEvent;
import org.example.events.UserDeletedEvent;
import org.example.emailnotificationmicroservice.exception.NonRetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@KafkaListener(topics = {"user-created-events-topic", "user-deleted-events-topic"}, groupId = "user-events")
public class UserEventsHandler {

    @KafkaHandler
    public void handle(UserCreatedEvent userCreatedEvent){

        log.info("Received event: {}", userCreatedEvent.getClass().getName());

        try {

            log.info("Sending email notification of user {} creation to {}",
                    userCreatedEvent.getName(),
                    userCreatedEvent.getEmail());


            log.info("Email notification of user {} creation to {} successfully sent",
                    userCreatedEvent.getName(),
                    userCreatedEvent.getEmail());

        }catch (Exception e){
            log.error("Error while sending email notification occured: {}", e.getMessage());
            throw new NonRetryableException(e);
        }
    }

    @KafkaHandler
    public void handle(UserDeletedEvent userDeletedEvent){

        log.info("Received event: {}", userDeletedEvent.getClass().getName());

        try {

            log.info("Sending email notification to {} of deleting user with id={}",
                    userDeletedEvent.getEmail(),
                    userDeletedEvent.getId());


            log.info("Email notification to {} of deleting user with id={} successfully sent",
                    userDeletedEvent.getEmail(),
                    userDeletedEvent.getId());

        }catch (Exception e){
            log.error("Error while sending email notification occured: {}", e.getMessage());
            throw new NonRetryableException(e);
        }
    }


}
