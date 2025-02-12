package org.xiyu.yee.capespringboot.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.xiyu.yee.capespringboot.model.RegistrationRequest;
import org.xiyu.yee.capespringboot.service.UserService;
import org.xiyu.yee.capespringboot.repository.RegistrationRequestRepository;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/register")
public class RegistrationController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RegistrationRequestRepository registrationRequestRepository;
    
    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationRequest", new RegistrationRequest());
        return "register";
    }
    
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String url) {
        try {
            System.out.println("Received registration request for: " + username);

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
            registrationRequest.setStatus("PENDING");
            registrationRequest.setCreatedAt(LocalDateTime.now());

            // 提交注册申请
            userService.submitRegistration(registrationRequest);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "注册申请已提交，请等待管理员审核"
            ));
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        }
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public String showPendingRegistrations(Model model) {
        model.addAttribute("requests", userService.getPendingRegistrations());
        return "pending-registrations";
    }
    
    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveRegistration(@PathVariable Long id) {
        try {
            // 检查请求是否存在
            RegistrationRequest request = registrationRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("注册申请不存在"));
            
            // 检查用户名是否已存在
            if (userService.findByUsername(request.getUsername()) != null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "该用户名已被注册"));
            }

            userService.approveRegistration(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "注册申请已通过"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    @PostMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectRegistration(@PathVariable Long id) {
        try {
            // 检查请求是否存在
            if (!registrationRequestRepository.existsById(id)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "注册申请不存在"));
            }

            userService.rejectRegistration(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "已拒绝注册申请"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
} 