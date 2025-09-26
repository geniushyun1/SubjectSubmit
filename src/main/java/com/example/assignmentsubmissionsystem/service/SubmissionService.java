package com.example.assignmentsubmissionsystem.service;

import com.example.assignmentsubmissionsystem.entity.Assignment;
import com.example.assignmentsubmissionsystem.entity.Submission;
import com.example.assignmentsubmissionsystem.entity.User;
import com.example.assignmentsubmissionsystem.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubmissionService {
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private AssignmentService assignmentService;
    
    public Submission createSubmission(Submission submission) {
        submission.setSubmittedAt(LocalDateTime.now());
        return submissionRepository.save(submission);
    }
    
    public List<Submission> findAllSubmissions() {
        return submissionRepository.findAll();
    }
    
    public List<Submission> findSubmissionsByStudent(User student) {
        return submissionRepository.findByStudent(student);
    }
    
    public List<Submission> findSubmissionsByAssignment(Assignment assignment) {
        return submissionRepository.findByAssignment(assignment);
    }
    
    public List<Submission> findSubmissionsByAssignmentId(Long assignmentId) {
        return submissionRepository.findByAssignmentIdOrderBySubmittedAtDesc(assignmentId);
    }
    
    public List<Submission> findSubmissionsByStudentId(Long studentId) {
        return submissionRepository.findByStudentIdOrderBySubmittedAtDesc(studentId);
    }
    
    public Optional<Submission> findByStudentAndAssignment(User student, Assignment assignment) {
        return submissionRepository.findByStudentAndAssignment(student, assignment);
    }
    
    public List<Submission> findUngradedSubmissions() {
        return submissionRepository.findUngradedSubmissions();
    }
    
    public List<Submission> findUngradedSubmissionsByAssignmentId(Long assignmentId) {
        return submissionRepository.findUngradedSubmissionsByAssignmentId(assignmentId);
    }
    
    public Submission gradeSubmission(Long submissionId, Integer score, String feedback) {
        Optional<Submission> submissionOpt = submissionRepository.findById(submissionId);
        if (submissionOpt.isPresent()) {
            Submission submission = submissionOpt.get();
            submission.setScore(score);
            submission.setFeedback(feedback);
            submission.setGradedAt(LocalDateTime.now());
            return submissionRepository.save(submission);
        }
        throw new RuntimeException("제출물을 찾을 수 없습니다.");
    }
    
    public Optional<Submission> findById(Long id) {
        return submissionRepository.findById(id);
    }
    
    public void deleteSubmission(Long id) {
        submissionRepository.deleteById(id);
    }
    
    public boolean canSubmit(User student, Assignment assignment) {
        // 과제가 아직 마감되지 않았는지 확인
        return assignment.getDueDate().isAfter(LocalDateTime.now());
    }
}
