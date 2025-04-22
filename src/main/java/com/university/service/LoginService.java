package com.university.service;

import com.university.model.User;

import com.university.config.DatabaseConnection;
import com.university.model.User;
import com.university.model.Visitor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.mindrot.jbcrypt.BCrypt;

/**
 * @author GroupHDGs
 */
public class LoginService {
    public User authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password cannot be empty");
        }

        EntityManager em = DatabaseConnection.getEntityManager();
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class
            );
            query.setParameter("username", username);
            User user = query.getSingleResult();
            if (BCrypt.checkpw(password, user.getPassword())) {
                return user;
            }
            return null;
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public Visitor createVisitor() {
        return new Visitor();
    }
}