package com.example.assignmentsubmissionsystem.repository;

import com.example.assignmentsubmissionsystem.entity.Course;
import com.example.assignmentsubmissionsystem.entity.Enrollment;
import com.example.assignmentsubmissionsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentAndActiveTrue(User student);
    List<Enrollment> findByCourseAndActiveTrue(Course course);
    Optional<Enrollment> findByStudentAndCourse(User student, Course course);
}