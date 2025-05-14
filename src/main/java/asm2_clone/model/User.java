package asm2_clone.model;

public class User {
    private String username;
    private String password;
    private String role;
    private Person person;
    private String email;
    private String fullname;
    private String courseOrDept;

    public User(String username, String password, String role, Person person) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.person = person;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }
    public String getCourseOrDept() { return courseOrDept; }
    public void setCourseOrDept(String courseOrDept) { this.courseOrDept = courseOrDept; }
}