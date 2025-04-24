package com.university.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing JPA database connections.
 * @author GroupHDGs
 */
public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static final EntityManagerFactory emf;

    static {
        try {
            emf = Persistence.createEntityManagerFactory("UniversityPU");
            logger.info("EntityManagerFactory initialized successfully for UniversityPU");
        } catch (PersistenceException e) {
            logger.error("Failed to initialize EntityManagerFactory", e);
            throw new RuntimeException("Failed to initialize EntityManagerFactory", e);
        }
    }

    /**
     * Creates a new EntityManager instance.
     * @return a new EntityManager
     */
    public static EntityManager getEntityManager() {
        if (emf == null || !emf.isOpen()) {
            logger.error("EntityManagerFactory is not initialized or closed");
            throw new IllegalStateException("EntityManagerFactory is not available");
        }
        return emf.createEntityManager();
    }

    /**
     * Closes the EntityManagerFactory if open.
     */
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            logger.info("EntityManagerFactory closed");
        }
    }
}