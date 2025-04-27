package com.university.dao;

import com.university.config.DatabaseConnection;
import com.university.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.logging.Logger;

public class UserDao {
    private static final Logger LOGGER = Logger.getLogger(UserDao.class.getName());

    public User findByUsername(String username) {
        EntityManager em = DatabaseConnection.getEntityManager();
        try {
            LOGGER.info("Querying user by username: " + username);
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            LOGGER.warning("No user found for username: " + username);
            return null;
        } catch (Exception e) {
            LOGGER.severe("Error querying user: " + e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }
}