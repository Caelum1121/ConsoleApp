package com.university.service;

import com.university.config.DatabaseConnection;
import com.university.dao.UserDao;
import com.university.model.LoginRecord;
import com.university.model.User;
import com.university.model.Visitor;
import jakarta.persistence.EntityManager;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Service for handling user and visitor authentication.
 * @author GroupHDGs
 */
public class LoginService {
    private final UserDao userDao;

    public LoginService() {
        this.userDao = new UserDao();
    }

    public String authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            logLoginAttempt(null, null, username, false, "UNKNOWN");
            throw new IllegalArgumentException("Username and password cannot be empty");
        }

        User user = userDao.findByUsername(username);
        boolean success = false;
        String role = "UNKNOWN";

        if (user != null && BCrypt.checkpw(password, user.getPassword())) {
            success = true;
            role = user.getRole();
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
            return visitor;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
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
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to log login attempt", e);
        } finally {
            em.close();
        }
    }
}