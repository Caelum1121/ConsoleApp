package com.university.model;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Tracks login attempts in the university equipment lending system.
 * @author GroupHDGs
 */
@Entity
@Table(name = "login_records")
public class LoginRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "visitor_id")
    private Visitor visitor;

    @Column(name = "username")
    private String username;

    @Column(name = "login_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date loginTime;

    @Column(name = "success")
    private boolean success;

    @Column(name = "role")
    private String role;

    protected LoginRecord() {}

    public LoginRecord(User user, Visitor visitor, String username, boolean success, String role) {
        if (user != null && visitor != null) {
            throw new IllegalArgumentException("LoginRecord cannot have both user and visitor");
        }
        this.user = user;
        this.visitor = visitor;
        this.username = username;
        this.loginTime = new Date();
        this.success = success;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Visitor getVisitor() {
        return visitor;
    }

    public void setVisitor(Visitor visitor) {
        this.visitor = visitor;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "LoginRecord{username='" + username + "', success=" + success + ", role='" + role + "'}";
    }
}