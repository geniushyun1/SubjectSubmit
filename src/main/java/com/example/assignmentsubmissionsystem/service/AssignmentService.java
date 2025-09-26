package com.example.assignmentsubmissionsystem.service;

import com.example.assignmentsubmissionsystem.entity.Assignment;
import com.example.assignmentsubmissionsystem.entity.Course;
import com.example.assignmentsubmissionsystem.entity.User;
import com.example.assignmentsubmissionsystem.repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AssignmentService {
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    public Assignment createAssignment(Assignment assignment) {
        assignment.setCreatedAt(LocalDateTime.now());
        assignment.setUpdatedAt(LocalDateTime.now());
        return assignmentRepository.save(assignment);
    }
    
    public List<Assignment> findAllAssignments() {
        try {
            List<Assignment> assignments = assignmentRepository.findAllOrderByDueDate();
            return assignments != null ? assignments : new java.util.ArrayList<>();
        } catch (Exception e) {
            System.err.println("과제 목록 조회 서비스 오류: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    public List<Assignment> findActiveAssignments() {
        return assignmentRepository.findActiveAssignments(LocalDateTime.now());
    }
    
    public List<Assignment> findExpiredAssignments() {
        return assignmentRepository.findExpiredAssignments(LocalDateTime.now());
    }
    
    public Optional<Assignment> findById(Long id) {
        return assignmentRepository.findById(id);
    }
    
    public Assignment updateAssignment(Assignment assignment) {
        assignment.setUpdatedAt(LocalDateTime.now());
        return assignmentRepository.save(assignment);
    }
    
    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }
    
    public boolean isAssignmentActive(Assignment assignment) {
        return assignment.getDueDate().isAfter(LocalDateTime.now());
    }
    
    // 특정 수업의 과제 목록 조회
    public List<Assignment> findAssignmentsByCourse(Course course) {
        return assignmentRepository.findByCourseOrderByDueDateAsc(course);
    }
    
    // 학생이 등록한 수업의 과제 목록 조회
    public List<Assignment> findAssignmentsByStudent(User student) {
        return assignmentRepository.findByStudentEnrollments(student);
    }
    
    // 교수가 생성한 수업의 과제 목록 조회
    public List<Assignment> findAssignmentsByProfessor(User professor) {
        return assignmentRepository.findByProfessorCourses(professor);
    }
    
    // 특정 수업의 활성 과제 목록 조회
    public List<Assignment> findActiveAssignmentsByCourse(Course course) {
        return assignmentRepository.findActiveAssignmentsByCourse(course, LocalDateTime.now());
    }
}
