package org.xiyu.yee.capespringboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.xiyu.yee.capespringboot.repository.UserRepository;
import org.xiyu.yee.capespringboot.repository.RegistrationRequestRepository;
import org.xiyu.yee.capespringboot.model.RegistrationRequest;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import org.springframework.scheduling.annotation.Scheduled;
import org.xiyu.yee.capespringboot.security.LoginAttemptService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.xiyu.yee.capespringboot.model.User;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RegistrationRequestRepository registrationRequestRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private LoginAttemptService loginAttemptService;
    
    public String getUrlByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getUrl)
                .orElse(null);
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public String generateEncodedPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    @Transactional
    public RegistrationRequest submitRegistration(RegistrationRequest request) {
        try {
            // 验证用户名格式
            if (!request.getUsername().matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]{2,20}$")) {
                throw new RuntimeException("用户名必须是2-20位的中文、字母、数字或下划线");
            }
            
            // 检查用户名是否已存在
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new RuntimeException("该用户名已被注册，请更换其他用户名");
            }
            
            // 加密密码
            request.setPassword(passwordEncoder.encode(request.getPassword()));
            request.setStatus("PENDING");
            request.setCreatedAt(LocalDateTime.now());
            
            // 保存注册申请
            RegistrationRequest savedRequest = registrationRequestRepository.save(request);
            System.out.println("Successfully submitted registration request for: " + request.getUsername());
            return savedRequest;
        } catch (Exception e) {
            System.err.println("Failed to submit registration: " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public List<RegistrationRequest> getPendingRegistrations() {
        return registrationRequestRepository.findByStatus("PENDING");
    }
    
    @Transactional
    public void approveRegistration(Long requestId) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("注册申请不存在"));
        
        // 检查状态
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("该申请已被处理");
        }
        
        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("该用户名已被注册");
        }

        try {
            // 创建新用户
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setPassword(request.getPassword()); // 密码已经在提交时加密
            newUser.setUrl(request.getUrl());
            newUser.setRole("ROLE_USER");
            newUser.setEnabled(true);
            
            userRepository.save(newUser);
            
            // 删除注册申请
            registrationRequestRepository.delete(request);
            
            System.out.println("Successfully approved registration for: " + request.getUsername());
        } catch (Exception e) {
            throw new RuntimeException("处理注册申请时出错: " + e.getMessage());
        }
    }
    
    @Transactional
    public void rejectRegistration(Long requestId) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("注册申请不存在"));
        
        // 检查状态
        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("该申请已被处理");
        }

        try {
            // 直接删除注册申请
            registrationRequestRepository.delete(request);
            System.out.println("Successfully rejected registration for: " + request.getUsername());
        } catch (Exception e) {
            throw new RuntimeException("处理注册申请时出错: " + e.getMessage());
        }
    }
    
    @Transactional
    public void deleteUser(String username) {
        System.out.println("Attempting to delete user: " + username); // 添加日志
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        userRepository.delete(user);
        System.out.println("User deleted successfully: " + username);
    }
    
    @Transactional
    public void scheduleUserDeletion(String username, int minutes) {
        System.out.println("Scheduling deletion for user: " + username + " in " + minutes + " minutes"); // 添加日志
        userRepository.findByUsername(username).ifPresent(user -> {
            if (!user.getRole().equals("ROLE_ADMIN")) {
                user.setDeleteAt(LocalDateTime.now().plusMinutes(minutes));
                userRepository.save(user);
                System.out.println("Deletion scheduled successfully for: " + username);
            } else {
                System.out.println("Cannot schedule deletion for admin user: " + username);
                throw new RuntimeException("不能删除管理员账户");
            }
        });
    }
    
    @Transactional
    public void cancelScheduledDeletion(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setDeleteAt(null);
            userRepository.save(user);
        });
    }
    
    @Transactional
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");  // 默认创建普通用户
        return userRepository.save(user);
    }
    
    // 添加定时任务，检查并删除到期用户
    @Scheduled(fixedRate = 60000) // 每分钟检查一次
    public void checkAndDeleteScheduledUsers() {
        LocalDateTime now = LocalDateTime.now();
        List<User> users = userRepository.findByDeleteAtLessThanEqual(now);
        for (User user : users) {
            if (!user.getRole().equals("ROLE_ADMIN")) {
                userRepository.delete(user);
            }
        }
    }

    public User login(String username, String password) {
        try {
            if (loginAttemptService.isBlocked(username)) {
                throw new RuntimeException("账号已被临时锁定，请稍后再试");
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("该用户名尚未注册"));

            if (passwordEncoder.matches(password, user.getPassword())) {
                loginAttemptService.loginSucceeded(username);
                return user;
            }

            loginAttemptService.loginFailed(username);
            throw new RuntimeException("密码错误");
        } catch (Exception e) {
            System.err.println("Login error for user " + username + ": " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public boolean register(String username, String password) {
        try {
            // 检查用户名是否已存在
            if (userRepository.findByUsername(username).isPresent()) {
                System.out.println("Registration failed: Username already exists - " + username);
                return false;
            }

            // 创建注册申请
            RegistrationRequest request = new RegistrationRequest();
            request.setUsername(username);
            request.setPassword(password);
            request.setStatus("PENDING");
            request.setCreatedAt(LocalDateTime.now());

            // 提交注册申请
            submitRegistration(request);
            System.out.println("Registration request submitted successfully: " + username);
            return true;
        } catch (Exception e) {
            System.err.println("Registration error for user " + username + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("注册时发生错误: " + e.getMessage());
        }
    }
}

