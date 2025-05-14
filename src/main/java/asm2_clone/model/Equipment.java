package asm2_clone.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "equipment")
public class Equipment {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    @Column(name = "condition")
    private String condition;
    @Column(name = "status")
    private String status;
    @Column(name = "category")
    private String category;

    @Lob
    @Column(name = "image")
    private byte[] image;

    public Equipment() {}

    public Equipment(int id, String name, LocalDate purchaseDate, String condition, String status, String category) {
        this.id = id;
        this.name = name;
        this.purchaseDate = purchaseDate;
        this.condition = condition;
        this.status = status;
        this.category = category;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Equipment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", purchaseDate=" + purchaseDate +
                ", condition='" + condition + '\'' +
                ", status='" + status + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
} 