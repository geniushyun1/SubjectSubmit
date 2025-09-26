package com.example.assignmentsubmissionsystem.repository;

import com.example.assignmentsubmissionsystem.entity.Course;
import com.example.assignmentsubmissionsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    // 교수가 생성한 수업 목록 조회
    List<Course> findByProfessor(User professor);
    List<Course> findByProfessorOrderByCreatedAtDesc(User professor);
    
    // 수업 코드로 수업 조회
    Optional<Course> findByCourseCode(String courseCode);
    
    // 수업 코드 중복 확인
    boolean existsByCourseCode(String courseCode);
    
    // 학생이 등록한 수업 목록 조회
    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.student = :student AND e.active = true ORDER BY c.createdAt DESC")
    List<Course> findEnrolledCoursesByStudent(@Param("student") User student);
    
    // 특정 수업에 등록된 학생 수 조회
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course = :course AND e.active = true")
    long countActiveEnrollmentsByCourse(@Param("course") Course course);
}
