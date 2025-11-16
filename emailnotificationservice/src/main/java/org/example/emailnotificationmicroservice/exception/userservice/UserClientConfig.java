package org.example.emailnotificationmicroservice.exception.userservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Util;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class UserClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            try {
                if (response.body() == null) {
                    return new RuntimeException("No response body");
                }

                String body = Util.toString(response.body().asReader());

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.findAndRegisterModules();

                ErrorResponse error = objectMapper.readValue(body, ErrorResponse.class);

                if (response.status() == 404 && "NOT_FOUND".equals(error.code())) {
                    return new UserNotFoundException(error.message());
                }

                return new RuntimeException("Unhandled error: " + error.message());

            } catch (IOException e) {
                return new RuntimeException("Error decoding error response", e);
            }
        };
    }
}