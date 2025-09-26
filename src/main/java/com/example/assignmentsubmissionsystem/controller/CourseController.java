package com.example.assignmentsubmissionsystem.controller;

import com.example.assignmentsubmissionsystem.entity.Assignment;
import com.example.assignmentsubmissionsystem.entity.Course;
import com.example.assignmentsubmissionsystem.entity.Enrollment;
import com.example.assignmentsubmissionsystem.entity.User;
import com.example.assignmentsubmissionsystem.security.UserDetailsImpl;
import com.example.assignmentsubmissionsystem.service.AssignmentService;
import com.example.assignmentsubmissionsystem.service.CourseService;
import com.example.assignmentsubmissionsystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private AssignmentService assignmentService;

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
    @PostMapping("/professor/new")
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
        }
    }

    // 수업 상세 조회 (공통)
    @GetMapping("/{id}")
    public String courseDetail(@PathVariable Long id, Authentication authentication, Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User currentUser = userDetails.getUser();
        Course course = courseService.getCourseById(id).orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + id));

        boolean isProfessor = course.getProfessor().getId().equals(currentUser.getId());
        boolean isEnrolled = courseService.isStudentEnrolled(currentUser, course);

        if (!isProfessor && !isEnrolled) {
            return "redirect:/dashboard";
        }

        // 수업의 과제 목록 가져오기
        List<Assignment> assignments = assignmentService.findAssignmentsByCourse(course);
        
        // 등록된 학생 목록 가져오기 (Enrollment 객체로)
        List<Enrollment> enrolledStudents = courseService.getActiveEnrollmentsByCourse(course);

        model.addAttribute("course", course);
        model.addAttribute("isProfessor", isProfessor);
        model.addAttribute("assignments", assignments);
        model.addAttribute("enrolledStudents", enrolledStudents);
        model.addAttribute("studentCount", courseService.getEnrolledStudentCount(course));

        return "course/detail";
    }

    // 수업 수정 폼
    @GetMapping("/professor/edit/{id}")
    public String editCourseForm(@PathVariable Long id, Authentication authentication, Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User professor = userDetails.getUser();
        Course course = courseService.getCourseById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + id));

        if (!course.getProfessor().getId().equals(professor.getId())) {
            return "redirect:/courses/professor";
        }

        model.addAttribute("course", course);
        model.addAttribute("title", "수업 수정");
        return "professor/course-form";
    }

    // 수업 수정
    @PostMapping("/professor/edit/{id}")
    public String updateCourse(@PathVariable Long id, @Valid @ModelAttribute("course") Course course,
                              BindingResult bindingResult, Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User professor = userDetails.getUser();

        if (bindingResult.hasErrors()) {
            course.setId(id);
            return "professor/course-form";
        }
        
        Course existingCourse = courseService.getCourseById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + id));
        if (!existingCourse.getProfessor().getId().equals(professor.getId())) {
            return "redirect:/courses/professor";
        }
        
        existingCourse.setName(course.getName());
        existingCourse.setDescription(course.getDescription());
        existingCourse.setCourseCode(course.getCourseCode());
        
        courseService.updateCourse(existingCourse);
        redirectAttributes.addFlashAttribute("message", "수업 정보가 성공적으로 수정되었습니다.");
        return "redirect:/courses/professor";
    }

    // 수업 삭제
    @PostMapping("/professor/delete/{id}")
    public String deleteCourse(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User professor = userDetails.getUser();
        Course course = courseService.getCourseById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + id));
        
        if (!course.getProfessor().getId().equals(professor.getId())) {
            return "redirect:/courses/professor";
        }
        
        courseService.deleteCourse(id);
        redirectAttributes.addFlashAttribute("message", "수업이 성공적으로 삭제되었습니다.");
        return "redirect:/courses/professor";
    }

    // 교수: 학생을 수업에 추가
    @PostMapping("/{id}/add-student")
    public String addStudentToCourse(@PathVariable Long id,
                                     @RequestParam("username") String username,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User professor = userService.getUserByUsername(userDetails.getUsername());
        
        try {
            Course course = courseService.getCourseById(id).orElse(null);
            if (course == null || !course.getProfessor().getId().equals(professor.getId())) {
                redirectAttributes.addFlashAttribute("error", "수업을 찾을 수 없거나 권한이 없습니다.");
                return "redirect:/courses/" + id;
            }
            
            User student = userService.getUserByUsername(username);
            if (student.getRole() != User.Role.STUDENT) {
                redirectAttributes.addFlashAttribute("error", "해당 사용자는 학생이 아닙니다.");
                return "redirect:/courses/" + id;
            }
            
            courseService.enrollStudent(student, course);
            redirectAttributes.addFlashAttribute("message", "학생이 성공적으로 추가되었습니다.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "학생 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/courses/" + id;
    }
    
    // 교수: 학생을 수업에서 제거
    @PostMapping("/{id}/remove-student")
    public String removeStudentFromCourse(@PathVariable Long id,
                                          @RequestParam("studentId") Long studentId,
                                          Authentication authentication,
                                          RedirectAttributes redirectAttributes) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User professor = userService.getUserByUsername(userDetails.getUsername());
        
        try {
            Course course = courseService.getCourseById(id).orElse(null);
            if (course == null || !course.getProfessor().getId().equals(professor.getId())) {
                redirectAttributes.addFlashAttribute("error", "수업을 찾을 수 없거나 권한이 없습니다.");
                return "redirect:/courses/" + id;
            }
            
            User student = userService.findById(studentId).orElse(null);
            if (student == null) {
                redirectAttributes.addFlashAttribute("error", "학생을 찾을 수 없습니다.");
                return "redirect:/courses/" + id;
            }
            
            courseService.unenrollStudent(student, course);
            redirectAttributes.addFlashAttribute("message", "학생이 성공적으로 제거되었습니다.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "학생 제거 중 오류가 발생했습니다: " + e.getMessage());
        }
        
        return "redirect:/courses/" + id;
    }
}