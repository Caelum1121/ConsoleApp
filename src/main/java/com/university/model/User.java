package com.university.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "entity_id")
    private String entityId;

    // Constructors
    public User() {}

    public User(String id, String username, String password, String role, String entityId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.entityId = entityId;
    }

    public User(String username, String password, String role) {
        this.id = "u" + System.currentTimeMillis();
        this.username = username;
        this.password = password;
        this.role = role;
        this.entityId = null;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}