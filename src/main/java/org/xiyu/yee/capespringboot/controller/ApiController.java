package org.xiyu.yee.capespringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xiyu.yee.capespringboot.model.RegistrationRequest;
import org.xiyu.yee.capespringboot.model.User;
import org.xiyu.yee.capespringboot.service.UserService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    
    @Autowired
    private UserService userService;
    
    // GET 获取所有用户
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }
    
    // GET 获取单个用户
    @GetMapping("/users/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
            "username", user.getUsername(),
            "url", user.getUrl() != null ? user.getUrl() : ""
        ));
    }
    
    // POST 创建新用户
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> request) {
        try {
            System.out.println("Received create user request: " + request); // 添加日志
            
            String username = request.get("username");
            String password = request.get("password");
            String url = request.get("url");
            
            // 检查用户名是否已存在
            if (userService.findByUsername(username) != null) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "用户名已存在"
                    ));
            }

            // 创建新用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);  // UserService会处理密码加密
            user.setUrl(url);
            user.setRole("ROLE_USER");
            user.setEnabled(true);

            User savedUser = userService.createUser(user);
            System.out.println("User created successfully: " + savedUser.getUsername());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户添加成功"
            ));
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        }
    }
    
    // POST 注册新用户
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String url = request.get("url");
            
            System.out.println("Received registration request for: " + username);
            System.out.println("Request data: " + request); // 添加请求数据日志

            // 检查用户名是否已存在
            if (userService.findByUsername(username) != null) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "用户名已存在"
                    ));
            }

            // 检查用户名格式
            if (!username.matches("^[a-zA-Z0-9_\\u4e00-\\u9fa5]{2,20}$")) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "用户名必须是2-20位的中文、字母、数字或下划线"
                    ));
            }

            // 检查密码格式
            if (!password.matches("^[a-zA-Z0-9_]{6,20}$")) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "密码必须是6-20位的字母、数字或下划线"
                    ));
            }

            // 创建注册申请
            RegistrationRequest registrationRequest = new RegistrationRequest();
            registrationRequest.setUsername(username);
            registrationRequest.setPassword(password);
            registrationRequest.setUrl(url);

            // 提交注册申请
            userService.submitRegistration(registrationRequest);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "注册申请已提交，请等待管理员审核"
            ));
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace(); // 添加详细错误堆栈
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        }
    }
    
    // PUT 更新用户URL
    @PutMapping("/users/{username}")
    public ResponseEntity<?> updateUserUrl(
            @PathVariable String username,
            @RequestBody Map<String, String> body) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        String newUrl = body.get("url");
        if (newUrl == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "URL is required"));
        }
        
        user.setUrl(newUrl);
        User updatedUser = userService.saveUser(user);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "url", updatedUser.getUrl()
        ));
    }
    
    // DELETE 删除用户
    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        try {
            System.out.println("Delete request received for user: " + username);
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户不存在"
                ));
            }
            
            userService.deleteUser(username);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "用户已删除"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
    
    // POST 设置定时删除
    @PostMapping("/users/{username}/schedule-delete")
    public ResponseEntity<?> scheduleDelete(
            @PathVariable String username,
            @RequestBody Map<String, Integer> body) {
        try {
            Integer minutes = body.get("minutes");
            System.out.println("Schedule delete request for: " + username + ", minutes: " + minutes);
            
            if (minutes == null || minutes <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "请输入有效的时间"
                ));
            }
            
            userService.scheduleUserDeletion(username, minutes);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "已设置定时删除"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
} 