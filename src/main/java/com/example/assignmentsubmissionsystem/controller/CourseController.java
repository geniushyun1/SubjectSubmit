package com.example.assignmentsubmissionsystem.controller;

import com.example.assignmentsubmissionsystem.entity.Course;
import com.example.assignmentsubmissionsystem.entity.Enrollment;
import com.example.assignmentsubmissionsystem.entity.User;
import com.example.assignmentsubmissionsystem.security.UserDetailsImpl;
import com.example.assignmentsubmissionsystem.service.CourseService;
import com.example.assignmentsubmissionsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private UserService userService;
    
    // 교수: 수업 목록 조회
    @GetMapping("/professor")
    public String professorCourses(Authentication authentication, Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User professor = userService.getUserByUsername(userDetails.getUsername());
        List<Course> courses = courseService.getCoursesByProfessor(professor);
        model.addAttribute("courses", courses);
        return "professor/courses";
    }
    
    // 학생: 등록된 수업 목록 조회
    @GetMapping("/student")
    public String studentCourses(Authentication authentication, Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User student = userService.getUserByUsername(userDetails.getUsername());
        List<Course> courses = courseService.getEnrolledCoursesByStudent(student);
        model.addAttribute("courses", courses);
        return "student/courses";
    }
    
    // 수업 생성 폼
    @GetMapping("/professor/new")
    public String createCourseForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("title", "새 수업 생성");
        return "professor/course-form";
    }
    
    // 수업 생성
    @PostMapping("/professor")
    public String createCourse(@Valid @ModelAttribute("course") Course course,
                              BindingResult bindingResult,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "새 수업 생성");
            return "professor/course-form";
        }
        
        try {
            User professor = userService.getUserByUsername(userDetails.getUsername());
            course.setProfessor(professor);
            courseService.createCourse(course);
            redirectAttributes.addFlashAttribute("message", "수업이 성공적으로 생성되었습니다.");
            return "redirect:/courses/professor";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("courseCode", "error.course", e.getMessage());
            model.addAttribute("title", "새 수업 생성");
            return "professor/course-form";
        } catch (Exception e) {
            System.err.println("수업 생성 오류: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "수업 생성 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("title", "새 수업 생성");
            return "professor/course-form";
        }
    }
    
    // 수업 상세 조회
    @GetMapping("/{id}")
    public String courseDetail(@PathVariable Long id,
                              Authentication authentication,
                              Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userService.getUserByUsername(userDetails.getUsername());
        Course course = courseService.getCourseById(id).orElse(null);
        
        if (course == null) {
            return "redirect:/dashboard";
        }
        
        // 교수이거나 등록된 학생만 접근 가능
        boolean canAccess = course.getProfessor().getId().equals(currentUser.getId()) ||
                           courseService.isStudentEnrolled(currentUser, course);
        
        if (!canAccess) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("course", course);
        model.addAttribute("isProfessor", course.getProfessor().getId().equals(currentUser.getId()));
        model.addAttribute("enrolledStudents", courseService.getEnrolledStudentsByCourse(course));
        model.addAttribute("studentCount", courseService.getEnrolledStudentCount(course));
        
        return "course/detail";
    }
    
    // 수업 수정 폼
    @GetMapping("/{id}/edit")
    public String editCourseForm(@PathVariable Long id,
                                Authentication authentication,
                                Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User professor = userService.getUserByUsername(userDetails.getUsername());
        Course course = courseService.getCourseById(id).orElse(null);
        
        if (course == null || !course.getProfessor().getId().equals(professor.getId())) {
            return "redirect:/courses/professor";
        }
        
        model.addAttribute("course", course);
        return "professor/course-form";
    }
    
    // 수업 수정
    @PostMapping("/{id}/edit")
    public String updateCourse(@PathVariable Long id,
                              @Valid @ModelAttribute("course") Course course,
                              BindingResult bindingResult,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        if (bindingResult.hasErrors()) {
            return "professor/course-form";
        }
        
        try {
            User professor = userService.getUserByUsername(userDetails.getUsername());
            Course existingCourse = courseService.getCourseById(id).orElse(null);
            
            if (existingCourse == null || !existingCourse.getProfessor().getId().equals(professor.getId())) {
                return "redirect:/courses/professor";
            }
            
            // 기존 수업 정보 유지
            course.setId(id);
            course.setProfessor(professor);
            course.setCreatedAt(existingCourse.getCreatedAt());
            
            courseService.updateCourse(course);
            redirectAttributes.addFlashAttribute("message", "수업이 성공적으로 수정되었습니다.");
            return "redirect:/courses/professor";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("courseCode", "error.course", e.getMessage());
            return "professor/course-form";
        }
    }
    
    // 수업 삭제
    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User professor = userService.getUserByUsername(userDetails.getUsername());
        Course course = courseService.getCourseById(id).orElse(null);
        
        if (course != null && course.getProfessor().getId().equals(professor.getId())) {
            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("message", "수업이 성공적으로 삭제되었습니다.");
        }
        
        return "redirect:/courses/professor";
    }
    
    // 수업 등록 폼
    @GetMapping("/enroll")
    public String enrollForm(Model model) {
        return "student/enroll-form";
    }
    
    // 수업 등록
    @PostMapping("/enroll")
    public String enrollCourse(@RequestParam("courseCode") String courseCode,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        try {
            User student = userService.getUserByUsername(userDetails.getUsername());
            Course course = courseService.getCourseByCode(courseCode).orElse(null);
            
            if (course == null) {
                redirectAttributes.addFlashAttribute("error", "존재하지 않는 수업 코드입니다.");
                return "redirect:/courses/enroll";
            }
            
            courseService.enrollStudent(student, course);
            redirectAttributes.addFlashAttribute("message", "수업에 성공적으로 등록되었습니다.");
            return "redirect:/courses/student";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/courses/enroll";
        }
    }
    
    // 수업 등록 취소
    @PostMapping("/{id}/unenroll")
    public String unenrollCourse(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User student = userService.getUserByUsername(userDetails.getUsername());
        Course course = courseService.getCourseById(id).orElse(null);
        
        if (course != null) {
            courseService.unenrollStudent(student, course);
            redirectAttributes.addFlashAttribute("message", "수업 등록이 취소되었습니다.");
        }
        
        return "redirect:/courses/student";
    }
}
