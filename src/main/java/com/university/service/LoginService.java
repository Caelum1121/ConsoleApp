package com.university.service;

import com.university.config.DatabaseConnection;
import com.university.dao.UserDao;
import com.university.model.LoginRecord;
import com.university.model.User;
import com.university.model.Visitor;
import jakarta.persistence.EntityManager;
import org.mindrot.jbcrypt.BCrypt;
import java.util.logging.Logger;

/**
 * Service for handling user and visitor authentication.
 * @author GroupHDGs
 */
public class LoginService {
    private static final Logger LOGGER = Logger.getLogger(LoginService.class.getName());
    private final UserDao userDao;

    public LoginService() {
        this.userDao = new UserDao();
    }

    public String authenticate(String username, String password) {
        LOGGER.info("Attempting authentication for username: " + username);
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            LOGGER.warning("Invalid input: Username or password is empty");
            logLoginAttempt(null, null, username, false, "UNKNOWN");
            throw new IllegalArgumentException("Username and password cannot be empty");
        }

        User user = userDao.findByUsername(username);
        boolean success = false;
        String role = "UNKNOWN";

        if (user == null) {
            LOGGER.warning("User not found: " + username);
        } else if (password.equals(user.getPassword())) { // 直接比較明文
            success = true;
            role = user.getRole();
            LOGGER.info("Authentication successful for " + username + " with role: " + role);
        } else {
            LOGGER.warning("Password mismatch for username: " + username);
        }

        logLoginAttempt(user, null, username, success, role);
        return success ? role : null;
    }

    public Visitor createVisitor() {
        EntityManager em = DatabaseConnection.getEntityManager();
        try {
            em.getTransaction().begin();
            Visitor visitor = new Visitor();
            em.persist(visitor);
            logLoginAttempt(null, visitor, "visitor", true, "VISITOR");
            em.getTransaction().commit();
            LOGGER.info("Visitor created with ID: " + visitor.getId());
            return visitor;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.severe("Failed to create visitor: " + e.getMessage());
            throw new RuntimeException("Failed to create visitor", e);
        } finally {
            em.close();
        }
    }

    private void logLoginAttempt(User user, Visitor visitor, String username, boolean success, String role) {
        EntityManager em = DatabaseConnection.getEntityManager();
        try {
            em.getTransaction().begin();
            LoginRecord loginRecord = new LoginRecord(user, visitor, username, success, role);
            em.persist(loginRecord);
            em.getTransaction().commit();
            LOGGER.info("Login attempt logged: " + loginRecord);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            LOGGER.severe("Failed to log login attempt: " + e.getMessage());
            throw new RuntimeException("Failed to log login attempt", e);
        } finally {
            em.close();
        }
    }

    public User findByUsername(String username) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            LOGGER.warning("No user found for username: " + username);
        }
        return user;
    }
}