package com.example.assignmentsubmissionsystem.controller;

import com.example.assignmentsubmissionsystem.entity.Assignment;
import com.example.assignmentsubmissionsystem.entity.Course;
import com.example.assignmentsubmissionsystem.entity.Submission;
import com.example.assignmentsubmissionsystem.entity.User;
import com.example.assignmentsubmissionsystem.security.UserDetailsImpl;
import com.example.assignmentsubmissionsystem.service.AssignmentService;
import com.example.assignmentsubmissionsystem.service.CourseService;
import com.example.assignmentsubmissionsystem.service.SubmissionService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {
    
    @Autowired
    private AssignmentService assignmentService;
    
    @Autowired
    private CourseService courseService;
    
    @Autowired
    private SubmissionService submissionService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User student = userDetails.getUser();
            
            // 등록된 수업의 과제만 조회
            List<Course> enrolledCourses = courseService.getEnrolledCoursesByStudent(student);
            List<Assignment> activeAssignments = new java.util.ArrayList<>();
            
            for (Course course : enrolledCourses) {
                List<Assignment> courseAssignments = assignmentService.findAssignmentsByCourse(course);
                activeAssignments.addAll(courseAssignments);
            }
            
            List<Submission> mySubmissions = submissionService.findSubmissionsByStudent(student);
            
            if (mySubmissions == null) {
                mySubmissions = new java.util.ArrayList<>();
            }
            
            model.addAttribute("student", student);
            model.addAttribute("activeAssignments", activeAssignments);
            model.addAttribute("mySubmissions", mySubmissions);
            return "student/dashboard";
        } catch (Exception e) {
            System.err.println("학생 대시보드 조회 오류: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login";
        }
    }
    
    @GetMapping("/assignments")
    public String assignments(Authentication authentication, Model model) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User student = userDetails.getUser();
            
            // 등록된 수업의 과제만 조회
            List<Course> enrolledCourses = courseService.getEnrolledCoursesByStudent(student);
            List<Assignment> assignments = new java.util.ArrayList<>();
            
            for (Course course : enrolledCourses) {
                List<Assignment> courseAssignments = assignmentService.findAssignmentsByCourse(course);
                assignments.addAll(courseAssignments);
            }
            
            List<Submission> mySubmissions = submissionService.findSubmissionsByStudent(student);
            
            if (mySubmissions == null) {
                mySubmissions = new java.util.ArrayList<>();
            }
            
            model.addAttribute("assignments", assignments);
            model.addAttribute("mySubmissions", mySubmissions);
            model.addAttribute("student", student);
            return "student/assignments";
        } catch (Exception e) {
            System.err.println("학생 과제 목록 조회 오류: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("assignments", new java.util.ArrayList<>());
            model.addAttribute("mySubmissions", new java.util.ArrayList<>());
            model.addAttribute("error", "과제 목록을 불러오는 중 오류가 발생했습니다.");
            return "student/assignments";
        }
    }
    
    @GetMapping("/assignments/{id}")
    public String viewAssignment(@PathVariable Long id, Authentication authentication, Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User student = userDetails.getUser();
        
        Assignment assignment = assignmentService.findById(id)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다."));
        
        // 학생이 해당 수업에 등록되어 있는지 확인
        if (!courseService.isStudentEnrolled(student, assignment.getCourse())) {
            return "redirect:/student/dashboard?error=access_denied";
        }
        
        Submission mySubmission = submissionService.findByStudentAndAssignment(student, assignment)
                .orElse(null);
        
        boolean canSubmit = submissionService.canSubmit(student, assignment);
        
        model.addAttribute("assignment", assignment);
        model.addAttribute("mySubmission", mySubmission);
        model.addAttribute("canSubmit", canSubmit);
        model.addAttribute("student", student);
        return "student/assignment-detail";
    }
    
    @GetMapping("/assignments/{id}/submit")
    public String submitAssignment(@PathVariable Long id, Authentication authentication, Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User student = userDetails.getUser();
        
        Assignment assignment = assignmentService.findById(id)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다."));
        
        // 학생이 해당 수업에 등록되어 있는지 확인
        if (!courseService.isStudentEnrolled(student, assignment.getCourse())) {
            return "redirect:/student/dashboard?error=access_denied";
        }
        
        if (!submissionService.canSubmit(student, assignment)) {
            return "redirect:/student/assignments/" + id + "?error=deadline";
        }
        
        Submission existingSubmission = submissionService.findByStudentAndAssignment(student, assignment)
                .orElse(new Submission());
        
        model.addAttribute("assignment", assignment);
        model.addAttribute("submission", existingSubmission);
        return "student/submit-assignment";
    }
    
    @PostMapping("/assignments/{id}/submit")
    public String submitAssignment(@PathVariable Long id, 
                                 @RequestParam String content,
                                 @RequestParam(required = false) MultipartFile file,
                                 Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User student = userDetails.getUser();
        
        Assignment assignment = assignmentService.findById(id)
                .orElseThrow(() -> new RuntimeException("과제를 찾을 수 없습니다."));
        
        // 학생이 해당 수업에 등록되어 있는지 확인
        if (!courseService.isStudentEnrolled(student, assignment.getCourse())) {
            return "redirect:/student/dashboard?error=access_denied";
        }
        
        if (!submissionService.canSubmit(student, assignment)) {
            return "redirect:/student/assignments/" + id + "?error=deadline";
        }
        
        // 기존 제출물이 있는지 확인
        Submission existingSubmission = submissionService.findByStudentAndAssignment(student, assignment)
                .orElse(null);
        
        String fileName = null;
        String originalFileName = null;
        
        // 파일이 업로드된 경우 처리
        if (file != null && !file.isEmpty()) {
            // 기존 파일이 있다면 삭제
            if (existingSubmission != null && existingSubmission.getFileName() != null) {
                fileStorageService.deleteFile(existingSubmission.getFileName());
            }
            
            fileName = fileStorageService.storeFile(file);
            originalFileName = file.getOriginalFilename();
        }
        
        if (existingSubmission != null) {
            // 기존 제출물 업데이트
            existingSubmission.setContent(content);
            if (fileName != null) {
                existingSubmission.setFileName(fileName);
                existingSubmission.setOriginalFileName(originalFileName);
            }
            submissionService.createSubmission(existingSubmission);
        } else {
            // 새 제출물 생성
            Submission submission = new Submission(content, student, assignment);
            if (fileName != null) {
                submission.setFileName(fileName);
                submission.setOriginalFileName(originalFileName);
            }
            submissionService.createSubmission(submission);
        }
        
        return "redirect:/student/assignments/" + id + "?success=submitted";
    }
    
    @GetMapping("/submissions")
    public String mySubmissions(Authentication authentication, Model model) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User student = userDetails.getUser();
        
        List<Submission> submissions = submissionService.findSubmissionsByStudent(student);
        
        model.addAttribute("submissions", submissions);
        model.addAttribute("student", student);
        return "student/submissions";
    }
    
    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User student = userDetails.getUser();
            
            // 파일이 해당 학생의 제출물인지 확인
            boolean isAuthorized = submissionService.findSubmissionsByStudent(student)
                    .stream()
                    .anyMatch(submission -> fileName.equals(submission.getFileName()));
            
            if (!isAuthorized) {
                return ResponseEntity.notFound().build();
            }
            
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
