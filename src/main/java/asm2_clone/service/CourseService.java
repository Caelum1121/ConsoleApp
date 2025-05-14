package asm2_clone.service;

import asm2_clone.db.CourseDAO;
import asm2_clone.model.Course;
import java.util.List;

public class CourseService {
    private final CourseDAO courseDAO = new CourseDAO();

    public List<Course> getAllCourses() {
        return courseDAO.getAllCourses();
    }

    public boolean assignAcademicToCourse(String courseId, String academicId) {
        return courseDAO.assignAcademicToCourse(courseId, academicId);
    }
} 