package com.example.assignmentsubmissionsystem;

import com.example.assignmentsubmissionsystem.entity.Assignment;
import com.example.assignmentsubmissionsystem.entity.User;
import com.example.assignmentsubmissionsystem.service.AssignmentService;
import com.example.assignmentsubmissionsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@SpringBootApplication
public class AssignmentSubmissionSystemApplication implements CommandLineRunner {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AssignmentService assignmentService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(AssignmentSubmissionSystemApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 테스트 데이터 생성
        createTestData();
    }

    private void createTestData() {
        // 교수 계정 생성
        if (!userService.existsByUsername("professor")) {
            User professor = new User("professor", passwordEncoder.encode("password123"), "professor@university.edu", "김교수", User.Role.PROFESSOR);
            userService.createUser(professor);
            System.out.println("교수 계정이 생성되었습니다: professor / password123");
        }

        // 학생 계정들 생성
        if (!userService.existsByUsername("student1")) {
            User student1 = new User("student1", passwordEncoder.encode("password123"), "student1@university.edu", "이학생", User.Role.STUDENT);
            userService.createUser(student1);
            System.out.println("학생 계정이 생성되었습니다: student1 / password123");
        }

        if (!userService.existsByUsername("student2")) {
            User student2 = new User("student2", passwordEncoder.encode("password123"), "student2@university.edu", "박학생", User.Role.STUDENT);
            userService.createUser(student2);
            System.out.println("학생 계정이 생성되었습니다: student2 / password123");
        }

        if (!userService.existsByUsername("student3")) {
            User student3 = new User("student3", passwordEncoder.encode("password123"), "student3@university.edu", "최학생", User.Role.STUDENT);
            userService.createUser(student3);
            System.out.println("학생 계정이 생성되었습니다: student3 / password123");
        }

        // // 테스트 과제 생성
        // if (assignmentService.findAllAssignments().isEmpty()) {
        //     // 진행 중인 과제
        //     Assignment assignment1 = new Assignment(
        //         "Spring Boot 웹 애플리케이션 개발",
        //         "Spring Boot를 사용하여 RESTful API를 구현하는 과제입니다.\n\n요구사항:\n1. 사용자 인증/인가 기능\n2. CRUD 기능 구현\n3. 데이터베이스 연동\n4. API 문서화\n\n제출물:\n- 소스코드 (GitHub 링크)\n- 실행 방법 문서\n- API 테스트 결과",
        //         LocalDateTime.now().plusDays(7)
        //     );
        //     assignmentService.createAssignment(assignment1);

        //     // 마감된 과제
        //     Assignment assignment2 = new Assignment(
        //         "데이터베이스 설계 및 구현",
        //         "학생 관리 시스템을 위한 데이터베이스 설계 과제입니다.\n\n요구사항:\n1. ERD 작성\n2. 정규화 과정 설명\n3. SQL 스크립트 작성\n4. 성능 최적화 방안",
        //         LocalDateTime.now().minusDays(3)
        //     );
        //     assignmentService.createAssignment(assignment2);

        //     // 곧 마감될 과제
        //     Assignment assignment3 = new Assignment(
        //         "알고리즘 문제 해결",
        //         "주어진 알고리즘 문제들을 해결하는 과제입니다.\n\n문제:\n1. 이진 탐색 구현\n2. 정렬 알고리즘 비교\n3. 동적 프로그래밍 문제\n4. 그래프 탐색 알고리즘",
        //         LocalDateTime.now().plusHours(2)
        //     );
        //     assignmentService.createAssignment(assignment3);

        //     System.out.println("테스트 과제가 생성되었습니다.");
        // }
    }
}
