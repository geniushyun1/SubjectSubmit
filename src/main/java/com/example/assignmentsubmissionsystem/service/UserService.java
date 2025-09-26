package com.example.assignmentsubmissionsystem.service;

import com.example.assignmentsubmissionsystem.entity.User;
import com.example.assignmentsubmissionsystem.repository.UserRepository;
import com.example.assignmentsubmissionsystem.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User createUser(User user) {
        try {
            // 비밀번호를 BCrypt로 인코딩
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);
            System.out.println("사용자 생성 성공: " + savedUser.getUsername() + " (ID: " + savedUser.getId() + ")");
            return savedUser;
        } catch (Exception e) {
            System.err.println("사용자 생성 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> findStudents() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.STUDENT)
                .toList();
    }
    
    public List<User> findProfessors() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.Role.PROFESSOR)
                .toList();
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
        return new UserDetailsImpl(user);
    }
}
