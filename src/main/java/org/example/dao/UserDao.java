package org.example.dao;

import lombok.extern.java.Log;
import org.example.entity.User;
import org.example.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Log
public class UserDao {

    public Long save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Long id = (Long) session.save(user);
            transaction.commit();
            log.info("User saved successfully with ID: " + id);
            return id;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                log.warning("Transaction rolled back due to error");
            }
            log.log(Level.SEVERE, "Error saving user: " + e.getMessage(), e);
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            if (user != null) {
                log.info("User found with ID: " + id);
            } else {
                log.info("User not found with ID: " + id);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error finding user by ID " + id + ": " + e.getMessage(), e);
            throw new RuntimeException("Failed to find user by ID", e);
        }
    }

    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            List<User> users = query.list();
            log.info("Found " + users.size() + " users");
            return users;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error finding all users: " + e.getMessage(), e);
            throw new RuntimeException("Failed to find all users", e);
        }
    }

    public void update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(user);
            transaction.commit();
            log.info("User updated successfully with ID: " + user.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                log.warning("Transaction rolled back during update");
            }
            log.log(Level.SEVERE, "Error updating user with ID " + user.getId() + ": " + e.getMessage(), e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    public void delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
                log.info("User deleted successfully with ID: " + id);
            } else {
                log.warning("Attempted to delete non-existent user with ID: " + id);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
                log.warning("Transaction rolled back during delete");
            }
            log.log(Level.SEVERE, "Error deleting user with ID " + id + ": " + e.getMessage(), e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    public Optional<User> findByHometown(String hometown) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE hometown = :hometown", User.class);
            query.setParameter("hometown", hometown);
            User user = query.uniqueResult();
            if (user != null) {
                log.info("User found with hometown: " + hometown);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error finding user by hometown " + hometown + ": " + e.getMessage(), e);
            throw new RuntimeException("Failed to find user by hometown", e);
        }
    }
}