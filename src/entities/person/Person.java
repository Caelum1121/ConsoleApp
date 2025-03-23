package entities.person;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * @author Chang Fang Cih - s4073761
 */

public class Person {
    private String id;
    private String fullName;
    private Date dateOfBirth;
    private ContactInfo contactInfo;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    /**
     * Constructor for creating person objects
     * @param id Person's ID
     * @param fullName Person's full name
     * @param dateOfBirth Person's date of birth
     * @param phoneNumber Person's phone numbers
     * @param email Person's email address
     */
    public Person(String id, String fullName, Date dateOfBirth, String phoneNumber, String email) {
        this.id = id;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.contactInfo = new ContactInfo(phoneNumber, email);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    // Method to get formatted date of birth
    public String getFormattedDateOfBirth() {
        return dateFormat.format(dateOfBirth);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", dateOfBirth=" + getFormattedDateOfBirth() +
                ", contactInformation=" + contactInfo +
                '}';
    }
}
