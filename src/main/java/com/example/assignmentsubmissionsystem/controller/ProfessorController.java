package com.example.assignmentsubmissionsystem.controller;

import com.example.assignmentsubmissionsystem.entity.Assignment;
import com.example.assignmentsubmissionsystem.entity.Course;
import com.example.assignmentsubmissionsystem.entity.Submission;
import com.example.assignmentsubmissionsystem.entity.User;
import com.example.assignmentsubmissionsystem.security.UserDetailsImpl;
import com.example.assignmentsubmissionsystem.service.AssignmentService;
import com.example.assignmentsubmissionsystem.service.CourseService;
import com.example.assignmentsubmissionsystem.service.SubmissionService;
import com.example.assignmentsubmissionsystem.service.UserService;
import com.example.assignmentsubmissionsystem.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/professor")
public class ProfessorController {
    
    @Autowired
    private AssignmentService assignmentService;
    
    @Autowired
    private SubmissionService submissionService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private CourseService courseService;
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            List<Assignment> assignments = assignmentService.findAllAssignments();
            List<Submission> ungradedSubmissions = submissionService.findUngradedSubmissions();
            
            if (assignments == null) {
                assignments = new java.util.ArrayList<>();
            }
            if (ungradedSubmissions == null) {
                ungradedSubmissions = new java.util.ArrayList<>();
            }
            
            model.addAttribute("assignments", assignments);
            model.addAttribute("ungradedSubmissions", ungradedSubmissions);
            return "professor/dashboard";
        } catch (Exception e) {
            System.err.println("교수 대시보드 조회 오류: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("assignments", new java.util.ArrayList<>());
            model.addAttribute("ungradedSubmissions", new java.util.ArrayList<>());
            model.addAttribute("error", "대시보드를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "professor/dashboard";
        }
    }
    
    @GetMapping("/assignments")
    public String assignments(Model model) {
        try {
            List<Assignment> assignments = assignmentService.findAllAssignments();
            if (assignments == null) {
                assignments = new java.util.ArrayList<>();
            }
            model.addAttribute("assignments", assignments);
            return "professor/assignments";
        } catch (Exception e) {
            System.err.println("과제 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("assignments", new java.util.ArrayList<>());
            model.addAttribute("error", "과제 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "professor/assignments";
        }
    }
    
    @GetMapping("/assignments/new")
    public String newAssignment(@RequestParam(value = "courseId", required = false) Long courseId,
                               Authentication authentication,
                               Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User professor = userService.getUserByUsername(userDetails.getUsername());
        List<Course> courses = courseService.getCoursesByProfessor(professor);
        
        model.addAttribute("assignment", new Assignment());
        model.addAttribute("courses", courses);
        model.addAttribute("selectedCourseId", courseId);
        
        return "professor/assignment-form";
    }
    
    @PostMapping("/assignments")
    public String createAssignment(@ModelAttribute Assignment assignment,
                                  @RequestParam("courseId") Long courseId,
                                  Authentication authentication,
                                  Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        try {
            Course course = courseService.getCourseById(courseId).orElse(null);
            if (course == null) {
                User professor = userService.getUserByUsername(userDetails.getUsername());
                List<Course> courses = courseService.getCoursesByProfessor(professor);
                model.addAttribute("assignment", assignment);
                model.addAttribute("courses", courses);
                model.addAttribute("selectedCourseId", courseId);
                model.addAttribute("error", "존재하지 않는 수업입니다.");
                return "professor/assignment-form";
            }
            
            User professor = userService.getUserByUsername(userDetails.getUsername());
            assignment.setProfessor(professor);
            assignment.setCourse(course);
            assignmentService.createAssignment(assignment);
            
            return "redirect:/professor/assignments";
        } catch (Exception e) {
            System.err.println("과제 생성 오류: " + e.getMessage());
            e.printStackTrace();
            
            User professor = userService.getUserByUsername(userDetails.getUsername());
            List<Course> courses = courseService.getCoursesByProfessor(professor);
            model.addAttribute("assignment", assignment);
            model.addAttribute("courses", courses);
            model.addAttribute("error", "과제 생성 중 오류가 발생했습니다: " + e.getMessage());
            return "professor/assignment-form";
        }
    }
    
    @GetMapping("/assignments/{id}")
    public String viewAssignment(@PathVariable Long id, Model model) {
        Assignment assignment = assignmentService.findById(id)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다."));
        List<Submission> submissions = submissionService.findSubmissionsByAssignment(assignment);
        
        model.addAttribute("assignment", assignment);
        model.addAttribute("submissions", submissions);
        return "professor/assignment-detail";
    }
    
    @GetMapping("/assignments/{id}/edit")
    public String editAssignment(@PathVariable Long id, Model model) {
        Assignment assignment = assignmentService.findById(id)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다."));
        model.addAttribute("assignment", assignment);
        return "professor/assignment-form";
    }
    
    @PostMapping("/assignments/{id}")
    public String updateAssignment(@PathVariable Long id, @ModelAttribute Assignment assignment, Model model) {
        try {
            // 기존 과제 조회
            Assignment existingAssignment = assignmentService.findById(id)
                    .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다."));
            
            // 과제 정보 업데이트
            existingAssignment.setTitle(assignment.getTitle());
            existingAssignment.setDescription(assignment.getDescription());
            existingAssignment.setDueDate(assignment.getDueDate());
            
            // 서비스 호출
            assignmentService.updateAssignment(existingAssignment);
            
            return "redirect:/professor/assignments";
        } catch (Exception e) {
            System.err.println("과제 수정 오류: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("assignment", assignment);
            model.addAttribute("error", "과제 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "professor/assignment-form";
        }
    }
    
    @GetMapping("/submissions")
    public String submissions(Model model) {
        List<Submission> submissions = submissionService.findAllSubmissions();
        model.addAttribute("submissions", submissions);
        return "professor/submissions";
    }
    
    @GetMapping("/submissions/{id}/grade")
    public String gradeSubmission(@PathVariable Long id, Model model) {
        Submission submission = submissionService.findById(id)
                .orElseThrow(() -> new RuntimeException("제출물을 찾을 수 없습니다."));
        model.addAttribute("submission", submission);
        return "professor/grade-submission";
    }
    
    @PostMapping("/submissions/{id}/grade")
    public String gradeSubmission(@PathVariable Long id, 
                                @RequestParam Integer score, 
                                @RequestParam(required = false) String feedback) {
        submissionService.gradeSubmission(id, score, feedback);
        return "redirect:/professor/submissions";
    }
    
    @GetMapping("/students")
    public String students(Model model) {
        List<User> students = userService.findStudents();
        model.addAttribute("students", students);
        return "professor/students";
    }
    
    @PostMapping("/assignments/{id}/delete")
    public String deleteAssignment(@PathVariable Long id) {
        try {
            // 과제와 관련된 제출물들도 함께 삭제
            List<Submission> submissions = submissionService.findSubmissionsByAssignmentId(id);
            for (Submission submission : submissions) {
                // 제출물에 첨부된 파일이 있다면 삭제
                if (submission.getFileName() != null && !submission.getFileName().isEmpty()) {
                    try {
                        fileStorageService.deleteFile(submission.getFileName());
                    } catch (Exception e) {
                        System.err.println("파일 삭제 오류: " + e.getMessage());
                    }
                }
                // 제출물 삭제
                submissionService.deleteSubmission(submission.getId());
            }
            
            // 과제 삭제
            assignmentService.deleteAssignment(id);
            return "redirect:/professor/assignments?success=deleted";
        } catch (Exception e) {
            System.err.println("과제 삭제 오류: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/professor/assignments?error=delete_failed";
        }
    }
    
    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            // 교수는 모든 제출 파일에 접근 가능
            Path filePath = fileStorageService.loadFileAsResource(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
