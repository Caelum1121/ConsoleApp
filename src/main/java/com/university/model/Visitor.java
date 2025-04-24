package com.university.model;

import jakarta.persistence.*;

/**
 * Represents a non-authenticated visitor in the university equipment lending system.
 * @author GroupHDGs
 */
@Entity
@Table(name = "visitors")
public class Visitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    public Visitor() {}

    public Visitor(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Visitor{id=" + id + "}";
    }
}
