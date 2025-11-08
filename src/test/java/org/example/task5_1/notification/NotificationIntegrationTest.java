package org.example.task5_1.notification;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.example.task5_1.notification.dto.UserEvent;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.mail.internet.MimeMessage;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"users.events"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotificationIntegrationTest {

    private GreenMail greenMail;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private TestRestTemplate restTemplate;

    // Подставляем адреса брокера Kafka в свойства приложения
    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry, @Autowired EmbeddedKafkaBroker embeddedKafkaBroker) {
        registry.add("spring.kafka.bootstrap-servers", embeddedKafkaBroker::getBrokersAsString);
        // Consumer auto offset reset earliest for tests
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
    }

    @BeforeAll
    void setupGreenMail() {
        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();

        // Подменяем настройки mail на GreenMail
        System.setProperty("spring.mail.host", "localhost");
        System.setProperty("spring.mail.port", String.valueOf(ServerSetupTest.SMTP.getPort()));
        System.setProperty("spring.mail.protocol", "smtp");
    }

    @AfterAll
    void stopGreenMail() {
        if (greenMail != null) greenMail.stop();
    }

    @BeforeEach
    void beforeEach() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void whenUserCreated_eventLeadsToEmail() throws Exception {
        UserEvent event = new UserEvent(UserEvent.Operation.CREATE, "create-test@example.com");
        kafkaTemplate.send("users.events", event);

        // Ждем пока GreenMail примет письмо (до 5 секунд)
        boolean arrived = greenMail.waitForIncomingEmail(5000, 1);
        assertThat(arrived).isTrue();

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);
        String body = (String) messages[0].getContent();
        assertThat(body).contains("Ваш аккаунт на сайте");
    }

    @Test
    void whenUserDeleted_eventLeadsToEmail() throws Exception {
        UserEvent event = new UserEvent(UserEvent.Operation.DELETE, "delete-test@example.com");
        kafkaTemplate.send("users.events", event);

        boolean arrived = greenMail.waitForIncomingEmail(5000, 1);
        assertThat(arrived).isTrue();

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);
        String body = (String) messages[0].getContent();
        assertThat(body).contains("Ваш аккаунт был удалён");
    }

    @Test
    void restEndpoint_sendsEmail() throws Exception {
        UserEvent event = new UserEvent(UserEvent.Operation.CREATE, "rest-test@example.com");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserEvent> request = new HttpEntity<>(event, headers);

        ResponseEntity<Void> resp = restTemplate.postForEntity("/api/notifications/send", request, Void.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        boolean arrived = greenMail.waitForIncomingEmail(5000, 1);
        assertThat(arrived).isTrue();
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);
        String body = (String) messages[0].getContent();
        assertThat(body).contains("Ваш аккаунт на сайте");
    }
}