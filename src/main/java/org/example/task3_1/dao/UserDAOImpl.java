package org.example.task3_1.dao;

import org.example.task3_1.model.User;
import org.example.task3_1.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * DAO implementation for User. Added optional SessionFactory injection to facilitate testing
 * (if injectedSessionFactory is null, falls back to HibernateUtil.getSessionFactory()).
 */
public class UserDAOImpl implements UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    // Optional injected SessionFactory (used in tests). If null, fallback to HibernateUtil.
    private final SessionFactory injectedSessionFactory;

    // Default constructor preserves existing behavior (uses HibernateUtil)
    public UserDAOImpl() {
        this.injectedSessionFactory = null;
    }

    // New constructor to allow injecting a SessionFactory (for tests / alternate setups)
    public UserDAOImpl(SessionFactory sessionFactory) {
        this.injectedSessionFactory = sessionFactory;
    }

    // Helper to open a session from injected factory or HibernateUtil
    private Session openSession() {
        if (injectedSessionFactory != null) {
            return injectedSessionFactory.openSession();
        } else {
            return HibernateUtil.getSessionFactory().openSession();
        }
    }

    @Override
    public User create(User user) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
            return user;
        } catch (ConstraintViolationException cve) {
            if (tx != null) tx.rollback();
            logger.error("Constraint violation while creating user", cve);
            throw cve;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error creating user", e);
            throw e;
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = openSession()) {
            User user = session.find(User.class, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error finding user by id", e);
            throw e;
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = openSession()) {
            return session.createQuery("from User", User.class).list();
        } catch (Exception e) {
            logger.error("Error finding all users", e);
            throw e;
        }
    }

    @Override
    public User update(User user) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            session.merge(user);
            tx.commit();
            return user;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error updating user", e);
            throw e;
        }
    }

    @Override
    public boolean delete(Long id) {
        Transaction tx = null;
        try (Session session = openSession()) {
            tx = session.beginTransaction();
            User user = session.find(User.class, id);
            if (user == null) {
                return false;
            }
            session.remove(user);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            logger.error("Error deleting user", e);
            throw e;
        }
    }
}
