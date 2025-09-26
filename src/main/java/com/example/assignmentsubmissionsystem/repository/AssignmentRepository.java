package com.example.assignmentsubmissionsystem.repository;

import com.example.assignmentsubmissionsystem.entity.Assignment;
import com.example.assignmentsubmissionsystem.entity.Course;
import com.example.assignmentsubmissionsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    
    @Query("SELECT a FROM Assignment a ORDER BY a.dueDate ASC")
    List<Assignment> findAllOrderByDueDate();
    
    @Query("SELECT a FROM Assignment a WHERE a.dueDate > :currentTime ORDER BY a.dueDate ASC")
    List<Assignment> findActiveAssignments(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT a FROM Assignment a WHERE a.dueDate <= :currentTime ORDER BY a.dueDate DESC")
    List<Assignment> findExpiredAssignments(@Param("currentTime") LocalDateTime currentTime);
    
    // 특정 수업의 과제 목록 조회
    @Query("SELECT a FROM Assignment a WHERE a.course = :course ORDER BY a.dueDate ASC")
    List<Assignment> findByCourseOrderByDueDateAsc(@Param("course") Course course);
    
    // 학생이 등록한 수업의 과제 목록 조회
    @Query("SELECT a FROM Assignment a JOIN a.course c JOIN c.enrollments e WHERE e.student = :student AND e.active = true ORDER BY a.dueDate ASC")
    List<Assignment> findByStudentEnrollments(@Param("student") User student);
    
    // 교수가 생성한 수업의 과제 목록 조회
    @Query("SELECT a FROM Assignment a JOIN a.course c WHERE c.professor = :professor ORDER BY a.dueDate ASC")
    List<Assignment> findByProfessorCourses(@Param("professor") User professor);
    
    // 특정 수업의 활성 과제 목록 조회
    @Query("SELECT a FROM Assignment a WHERE a.course = :course AND a.dueDate > :currentTime ORDER BY a.dueDate ASC")
    List<Assignment> findActiveAssignmentsByCourse(@Param("course") Course course, @Param("currentTime") LocalDateTime currentTime);
}
