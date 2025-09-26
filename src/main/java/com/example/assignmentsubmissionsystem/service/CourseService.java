package com.example.assignmentsubmissionsystem.service;

import com.example.assignmentsubmissionsystem.entity.Course;
import com.example.assignmentsubmissionsystem.entity.Enrollment;
import com.example.assignmentsubmissionsystem.entity.User;
import com.example.assignmentsubmissionsystem.repository.CourseRepository;
import com.example.assignmentsubmissionsystem.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public Course createCourse(Course course) {
        // 수업 코드 중복 확인
        if (courseRepository.existsByCourseCode(course.getCourseCode())) {
            throw new IllegalArgumentException("이미 존재하는 수업 코드입니다: " + course.getCourseCode());
        }
        
        if (course.getCreatedAt() == null) {
            course.setCreatedAt(java.time.LocalDateTime.now());
        }
        if (course.getUpdatedAt() == null) {
            course.setUpdatedAt(java.time.LocalDateTime.now());
        }
        return courseRepository.save(course);
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getCoursesByProfessor(User professor) {
        return courseRepository.findByProfessor(professor);
    }

    public Course updateCourse(Course course) {
        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    public Optional<Course> findByCourseCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode);
    }

    public Enrollment enrollStudent(User student, Course course) {
        Optional<Enrollment> existingEnrollment = enrollmentRepository.findByStudentAndCourse(student, course);
        if (existingEnrollment.isPresent()) {
            Enrollment enrollment = existingEnrollment.get();
            if (!enrollment.isActive()) {
                enrollment.setActive(true); // Re-activate if previously inactive
                return enrollmentRepository.save(enrollment);
            }
            return enrollment; // Already enrolled and active
        } else {
            Enrollment enrollment = new Enrollment(student, course);
            return enrollmentRepository.save(enrollment);
        }
    }

    public void unenrollStudent(User student, Course course) {
        enrollmentRepository.findByStudentAndCourse(student, course).ifPresent(enrollment -> {
            enrollment.setActive(false); // Mark as inactive
            enrollmentRepository.save(enrollment);
        });
    }

    public List<Enrollment> getActiveEnrollmentsByStudent(User student) {
        return enrollmentRepository.findByStudentAndActiveTrue(student);
    }

    public List<Enrollment> getActiveEnrollmentsByCourse(Course course) {
        return enrollmentRepository.findByCourseAndActiveTrue(course);
    }

    public boolean isStudentEnrolled(User student, Course course) {
        return enrollmentRepository.findByStudentAndCourse(student, course)
                .map(Enrollment::isActive)
                .orElse(false);
    }

    public List<Course> getEnrolledCoursesByStudent(User student) {
        return getActiveEnrollmentsByStudent(student)
                .stream()
                .map(Enrollment::getCourse)
                .toList();
    }

    public List<User> getEnrolledStudentsByCourse(Course course) {
        return getActiveEnrollmentsByCourse(course)
                .stream()
                .map(Enrollment::getStudent)
                .toList();
    }

    public int getEnrolledStudentCount(Course course) {
        return getActiveEnrollmentsByCourse(course).size();
    }

    public Optional<Course> getCourseByCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode);
    }
}