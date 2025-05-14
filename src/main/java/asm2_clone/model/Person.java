package asm2_clone.model;

import java.util.Date;

public abstract class Person {
    protected String id;
    protected String fullName;
    protected String contactInfo;
    protected Date dateOfBirth;
    protected String password;

    public Person() {}

    public Person(String id, String fullName, String contactInfo, Date dateOfBirth) {
        this.id = id;
        this.fullName = fullName;
        this.contactInfo = contactInfo;
        this.dateOfBirth = dateOfBirth;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "Person{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
} 