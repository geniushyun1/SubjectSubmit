package com.example.assignmentsubmissionsystem.repository;

import com.example.assignmentsubmissionsystem.entity.Submission;
import com.example.assignmentsubmissionsystem.entity.User;
import com.example.assignmentsubmissionsystem.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    List<Submission> findByStudent(User student);
    
    List<Submission> findByAssignment(Assignment assignment);
    
    Optional<Submission> findByStudentAndAssignment(User student, Assignment assignment);
    
    @Query("SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId ORDER BY s.submittedAt DESC")
    List<Submission> findByAssignmentIdOrderBySubmittedAtDesc(@Param("assignmentId") Long assignmentId);
    
    @Query("SELECT s FROM Submission s WHERE s.student.id = :studentId ORDER BY s.submittedAt DESC")
    List<Submission> findByStudentIdOrderBySubmittedAtDesc(@Param("studentId") Long studentId);
    
    @Query("SELECT s FROM Submission s WHERE s.score IS NULL ORDER BY s.submittedAt ASC")
    List<Submission> findUngradedSubmissions();
    
    @Query("SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId AND s.score IS NULL ORDER BY s.submittedAt ASC")
    List<Submission> findUngradedSubmissionsByAssignmentId(@Param("assignmentId") Long assignmentId);
}
