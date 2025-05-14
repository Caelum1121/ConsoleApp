package asm2_clone.db;

import asm2_clone.model.AdminPerson;
import asm2_clone.model.Equipment;
import asm2_clone.model.Course;
import asm2_clone.model.AcademicStaff;
import asm2_clone.service.AdminService;
import asm2_clone.service.EquipmentService;
import asm2_clone.service.CourseService;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminTest {
    // User management
    private static final String TEST_USER_ID = "S990";
    private static AdminPerson testUser;
    private static AdminService adminService;

    // Equipment management
    private static EquipmentService equipmentService;
    private static Equipment testEquipment;

    // Course management
    private static CourseService courseService;
    private static Course testCourse;

    @BeforeAll
    static void setup() {
        adminService = new AdminService();
        equipmentService = new EquipmentService();
        courseService = new CourseService();

        // User
        testUser = new AdminPerson("AdminTest User", "Student", "admintestuser@example.com");
        testUser.setId(TEST_USER_ID);
        testUser.setDateOfBirth(LocalDate.of(2000, 1, 1));
        testUser.setCourseOrDept("TEST101");

        // Equipment
        testEquipment = new Equipment();
        testEquipment.setName("AdminTest Equipment");
        testEquipment.setCategory("Testing");
        testEquipment.setCondition("Brand New");
        testEquipment.setStatus("Available");
        testEquipment.setPurchaseDate(LocalDate.now());

        // --- CREATE SUPERVISOR (ACADEMIC STAFF) ---
        AcademicStaff supervisor = new AcademicStaff();
        supervisor.setId("A999");
        supervisor.setFullName("JUnit Supervisor");
        supervisor.setContactInfo("supervisor@example.com");
        supervisor.setDateOfBirth(java.sql.Date.valueOf(LocalDate.of(1980, 1, 1)));
        CourseDAO courseDAO = new CourseDAO();

        // Only add supervisor if not exists
        boolean supervisorExists = courseDAO.getAvailableAcademicStaff().stream()
            .anyMatch(staff -> "A999".equals(staff.getId()));
        if (!supervisorExists) {
            courseDAO.addAcademicStaff(supervisor);
        }

        // --- ADD COURSE TO DB ---
        testCourse = new Course();
        testCourse.setCourseId("TESTC01");
        testCourse.setCourseName("AdminTest Course");
        testCourse.setAcademicStaffId("A999"); // Set supervisor ID

        // Only add course if not exists
        boolean courseExists = courseDAO.getCoursesForAdminDashboard().stream()
            .anyMatch(c -> "TESTC01".equals(c.getCourseId()));
        if (!courseExists) {
            courseDAO.addCourse(testCourse);
        }
    }

    // --- USER PAGE TESTS ---
    @Test
    @Order(1)
    void testAddUser() {
        boolean added = adminService.addPerson(testUser);
        assertTrue(added, "Should add user successfully");
    }

    @Test
    @Order(2)
    void testGetAllUsers() {
        List<AdminPerson> people = adminService.getAllPeople();
        assertNotNull(people);
        assertTrue(people.stream().anyMatch(p -> TEST_USER_ID.equals(p.getId())));
    }

    @Test
    @Order(3)
    void testUpdateUser() {
        testUser.setName("AdminTest User Updated");
        adminService.updatePerson(testUser);
        List<AdminPerson> people = adminService.getAllPeople();
        assertTrue(people.stream().anyMatch(p -> "AdminTest User Updated".equals(p.getName())));
    }

    @Test
    @Order(4)
    void testDeleteUser() {
        adminService.deletePerson(testUser);
        List<AdminPerson> people = adminService.getAllPeople();
        assertFalse(people.stream().anyMatch(p -> TEST_USER_ID.equals(p.getId())));
    }

    // --- EQUIPMENT PAGE TESTS ---
    @Test
    @Order(5)
    void testAddEquipment() {
        boolean added = equipmentService.addNewEquipment(testEquipment, null); // null for image file
        assertTrue(added, "Should add equipment successfully");
    }

    @Test
    @Order(6)
    void testGetAllEquipment() {
        List<Equipment> equipmentList = equipmentService.getAllEquipment();
        assertNotNull(equipmentList);
        assertTrue(equipmentList.stream().anyMatch(e -> "AdminTest Equipment".equals(e.getName())));
    }

    @Test
    @Order(7)
    void testUpdateEquipment() {
        List<Equipment> equipmentList = equipmentService.getAllEquipment();
        Equipment eq = equipmentList.stream().filter(e -> "AdminTest Equipment".equals(e.getName())).findFirst().orElse(null);
        assertNotNull(eq);
        eq.setCondition("Good");
        boolean updated = equipmentService.updateEquipment(eq);
        assertTrue(updated, "Should update equipment");
    }

    @Test
    @Order(8)
    void testDeleteEquipment() {
        List<Equipment> equipmentList = equipmentService.getAllEquipment();
        Equipment eq = equipmentList.stream().filter(e -> "AdminTest Equipment".equals(e.getName())).findFirst().orElse(null);
        assertNotNull(eq);
        boolean deleted = equipmentService.deleteEquipment(eq.getId());
        assertTrue(deleted, "Should delete equipment");
    }

    // --- COURSE PAGE TESTS ---
    @Test
    @Order(9)
    void testAssignAcademicToCourse() {
        boolean assigned = courseService.assignAcademicToCourse(testCourse.getCourseId(), "A999");
        assertTrue(assigned, "Should assign academic to course");
    }

    @Test
    @Order(10)
    void testGetAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        assertNotNull(courses);
        // This only checks that the call works; you can expand as needed
    }

    // --- BORROWING PAGE TESTS ---
    @Test
    @Order(11)
    void testGetAllBorrowingRecords() {
        List<?> records = asm2_clone.db.LendingRecordDAO.getAllRecords();
        assertNotNull(records);
        // Just check that the call works and returns a list
    }

    // --- STATISTICS PAGE TESTS ---
    @Test
    @Order(12)
    void testStatistics() {
        int totalUsers = asm2_clone.db.StatisticsDAO.getTotalUsers();
        int totalEquipment = asm2_clone.db.StatisticsDAO.getTotalEquipment();
        assertTrue(totalUsers >= 0, "Total users should be non-negative");
        assertTrue(totalEquipment >= 0, "Total equipment should be non-negative");
    }

    @AfterAll
    static void cleanup() {
        // Clean up test supervisor and test course
        CourseDAO courseDAO = new CourseDAO();
        AdminDAO adminDAO = new AdminDAO();
        try {
            courseDAO.deleteCourse("TESTC01"); // Remove the test course
        } catch (Exception e) {
            System.out.println("Could not delete test course: " + e.getMessage());
        }
        try {
            // Remove the test supervisor (academic staff)
            AdminPerson supervisorPerson = new AdminPerson("JUnit Supervisor", "Academic Staff", "supervisor@example.com");
            supervisorPerson.setId("A999");
            adminDAO.deletePerson(supervisorPerson);
        } catch (Exception e) {
            System.out.println("Could not delete test supervisor: " + e.getMessage());
        }
    }
}
