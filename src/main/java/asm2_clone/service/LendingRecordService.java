package asm2_clone.service;

import asm2_clone.db.LendingRecordDAO;
import asm2_clone.model.*;
import java.util.List;
import java.util.Map;

public class LendingRecordService {
    public List<LendingRecord> getRecordsByStudentId(String studentId) {
        return LendingRecordDAO.getRecordsByStudentId(studentId);
    }

    public Map<String, Map<String, Integer>> getSeparatedStatsByAcademicId(String academicId) {
        return LendingRecordDAO.getSeparatedStatsByAcademicId(academicId);
    }
    public Map<String, Integer> getBorrowingFrequencyByCourse(String academicId) {
        return LendingRecordDAO.getBorrowingFrequencyByCourse(academicId);
    }
    public Map<String, Map<String, Integer>> getBorrowingStatusByCourse(String academicId) {
        return LendingRecordDAO.getBorrowingStatusByCourse(academicId);
    }

} 