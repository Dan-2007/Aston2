package org.example.task3_1.dao;

import org.example.task3_1.model.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class UserDAOImplIT {

    private PostgreSQLContainer<?> postgres;
    private SessionFactory sessionFactory;
    private UserDAOImpl userDao;

    @BeforeEach
    void setUp() {
        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:18-alpine"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        postgres.start();

        Properties props = new Properties();
        props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.put("hibernate.connection.url", postgres.getJdbcUrl());
        props.put("hibernate.connection.username", postgres.getUsername());
        props.put("hibernate.connection.password", postgres.getPassword());
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.hbm2ddl.auto", "create-drop"); // drop table each run
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");

        Configuration cfg = new Configuration();
        cfg.addProperties(props);
        cfg.addAnnotatedClass(User.class);

        sessionFactory = cfg.buildSessionFactory();


        userDao = new UserDAOImpl(sessionFactory);
    }

    @AfterEach
    void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (postgres != null) {
            postgres.stop();
        }
    }

    @Test
    void testCreateAndFindById() {
        User user = new User();
        user.setName("Ivan");
        user.setEmail("ivan@example.com");
        user.setCreatedAt(LocalDateTime.now());

        User created = userDao.create(user);
        assertNotNull(created);
        assertNotNull(created.getId());

        Optional<User> found = userDao.findById(created.getId());
        assertTrue(found.isPresent());
        assertEquals("Ivan", found.get().getName());
        assertEquals("ivan@example.com", found.get().getEmail());
    }

    @Test
    void testFindAll() {
        List<User> before = userDao.findAll();
        assertNotNull(before);

        User u1 = new User();
        u1.setName("A");
        u1.setEmail("a@example.com");
        u1.setCreatedAt(LocalDateTime.now());
        userDao.create(u1);

        User u2 = new User();
        u2.setName("B");
        u2.setEmail("b@example.com");
        u2.setCreatedAt(LocalDateTime.now());
        userDao.create(u2);

        List<User> all = userDao.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testUpdateAndDelete() {
        User user = new User();
        user.setName("Petr");
        user.setEmail("petr@example.com");
        user.setCreatedAt(LocalDateTime.now());

        User created = userDao.create(user);
        Long id = created.getId();
        assertNotNull(id);

        // Update
        created.setName("Petr Updated");
        userDao.update(created);

        Optional<User> updated = userDao.findById(id);
        assertTrue(updated.isPresent());
        assertEquals("Petr Updated", updated.get().getName());

        // Delete
        boolean deleted = userDao.delete(id);
        assertTrue(deleted);

        Optional<User> afterDelete = userDao.findById(id);
        assertFalse(afterDelete.isPresent());
    }
}
