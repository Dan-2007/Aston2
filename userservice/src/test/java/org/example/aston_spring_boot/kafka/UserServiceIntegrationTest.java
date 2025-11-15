package org.example.aston_spring_boot.kafka;

import org.example.aston_spring_boot.dto.UserCreateDTO;
import org.example.aston_spring_boot.dto.UserDTO;
import org.example.aston_spring_boot.mapper.UserMapper;
import org.example.aston_spring_boot.model.User;
import org.example.aston_spring_boot.repository.UserRepository;
import org.example.aston_spring_boot.service.UserService;
import org.example.aston_spring_boot.specification.UserSpecification;
import org.example.events.UserCreatedEvent;
import org.example.events.UserDeletedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("kafka-test")
@EmbeddedKafka(partitions = 3, count = 3, controlledShutdown = true)
@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private UserSpecification userSpecification;

    private UserCreateDTO userCreateDTO;
    private User user;
    private UserDTO userDTO;

    @Autowired
    private Environment environment;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private KafkaMessageListenerContainer<String, UserCreatedEvent> createdEventsContainer;
    private KafkaMessageListenerContainer<String, UserDeletedEvent> deletedEventsContainer;


    private BlockingQueue<ConsumerRecord<String, UserCreatedEvent>> createdEventsRecords;
    private BlockingQueue<ConsumerRecord<String, UserDeletedEvent>> deletedEventsRecords;

    @BeforeAll
    void setUp(){
        // Consumer factory
        DefaultKafkaConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerProperties());

        // Consumer для created events
        ContainerProperties containerProperties = new ContainerProperties(environment.getProperty("app.kafka.topics.user-created"));
        createdEventsContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        createdEventsRecords = new LinkedBlockingQueue<>();
        createdEventsContainer.setupMessageListener((MessageListener<String, UserCreatedEvent>) createdEventsRecords::add);
        createdEventsContainer.start();

        // Consumer для deleted events
        ContainerProperties deletedEventsContainerProperties = new ContainerProperties(environment.getProperty("app.kafka.topics.user-deleted"));
        deletedEventsContainer = new KafkaMessageListenerContainer<>(consumerFactory, deletedEventsContainerProperties);
        deletedEventsRecords = new LinkedBlockingQueue<>();
        deletedEventsContainer.setupMessageListener((MessageListener<String, UserDeletedEvent>) deletedEventsRecords::add);
        deletedEventsContainer.start();

        ContainerTestUtils.waitForAssignment(createdEventsContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        ContainerTestUtils.waitForAssignment(deletedEventsContainer, embeddedKafkaBroker.getPartitionsPerTopic());
    }

    @BeforeEach
    void setUpMocks() {
        userCreateDTO = UserCreateDTO.builder()
                .name("John")
                .email("john@gmail.com")
                .age(35)
                .build();

        user = User.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .age(35)
                .build();

        userDTO = UserDTO.builder()
                .id(1L)
                .name("John")
                .email("john@gmail.com")
                .age(35)
                .createdAt(LocalDateTime.now())
                .build();

        reset(userMapper, userRepository);

        when(userMapper.map(userCreateDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.map(user)).thenReturn(userDTO);
    }



    @Test
    void testCreateUser_whenGivenValidUserDetails_successfullySendsKafkaMessage() throws InterruptedException {
        // Arrange в BeforeEach

        // Act
        userService.createUser(userCreateDTO);

        // Assert
        ConsumerRecord<String, UserCreatedEvent> message = createdEventsRecords.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        assertNotNull(message.key());
        UserCreatedEvent userCreatedEvent = message.value();
        assertEquals(userCreateDTO.getName(), userCreatedEvent.getName());
        assertEquals(userCreateDTO.getEmail(), userCreatedEvent.getEmail());
        assertEquals(userCreateDTO.getAge(), userCreatedEvent.getAge());
    }

    @Test
    void testDeleteUser_whenUserExists_successfullySendsDeleteEvent() throws InterruptedException {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        userService.deleteUser(userId);

        // Assert
        ConsumerRecord<String, UserDeletedEvent> message = deletedEventsRecords.poll(3000, TimeUnit.MILLISECONDS);
        assertNotNull(message);
        assertEquals(userId.toString(), message.key());
        UserDeletedEvent userDeletedEvent = message.value();
        assertNotNull(userDeletedEvent);
        assertEquals(userId, userDeletedEvent.getId());
        assertEquals(user.getEmail(), userDeletedEvent.getEmail());
        assertNotNull(userDeletedEvent.getEventId());
        assertNotNull(userDeletedEvent.getEventTimestamp());
    }

    private Map<String, Object> getConsumerProperties(){

        return Map.of(

        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),

        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,

        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,

        ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,

        ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id"),

        JsonDeserializer.TRUSTED_PACKAGES,
        environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"),

        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("spring.kafka.consumer.auto-offset-reset")

        );
    }

    @AfterAll
    void tearDown(){
        createdEventsContainer.stop();
    }
}
