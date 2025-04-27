package com.university.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static EntityManagerFactory emf;

    static {
        try {
            LOGGER.info("Initializing EntityManagerFactory for persistence unit: UniversityPU");
            emf = Persistence.createEntityManagerFactory("UniversityPU");
            LOGGER.info("EntityManagerFactory initialized successfully");
        } catch (Throwable t) {
            LOGGER.severe("Failed to initialize EntityManagerFactory: " + t.getMessage());
            t.printStackTrace();
            throw new RuntimeException("Failed to initialize EntityManagerFactory", t);
        }
    }

    public static EntityManager getEntityManager() {
        if (emf == null || !emf.isOpen()) {
            LOGGER.warning("EntityManagerFactory is null or closed, attempting to reinitialize");
            try {
                emf = Persistence.createEntityManagerFactory("UniversityPU");
                LOGGER.info("EntityManagerFactory reinitialized successfully");
            } catch (Throwable t) {
                LOGGER.severe("Failed to reinitialize EntityManagerFactory: " + t.getMessage());
                t.printStackTrace();
                throw new RuntimeException("Failed to reinitialize EntityManagerFactory", t);
            }
        }
        EntityManager em = emf.createEntityManager();
        LOGGER.info("Created new EntityManager");
        return em;
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            LOGGER.info("EntityManagerFactory closed");
        }
    }
}
