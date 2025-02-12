package org.xiyu.yee.capespringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.xiyu.yee.capespringboot.model.User;
import org.xiyu.yee.capespringboot.security.LoginAttemptService;
import org.xiyu.yee.capespringboot.service.UserService;
import org.xiyu.yee.capespringboot.repository.UserRepository;
import java.security.Principal;
import org.xiyu.yee.capespringboot.config.AnnouncementConfig;
import org.xiyu.yee.capespringboot.repository.RegistrationRequestRepository;
import java.util.List;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Arrays;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

@Controller
public class WebController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AnnouncementConfig announcementConfig;
    
    @Autowired
    private RegistrationRequestRepository registrationRequestRepository;
    
    @Autowired
    private LoginAttemptService loginAttemptService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @ModelAttribute("announcements")
    public List<String> getAnnouncements() {
        String[] announcementArray = announcementConfig.getAnnouncements().split("\\|\\|");
        return Arrays.asList(announcementArray);
    }
    
    @ModelAttribute("pendingRegistrations")
    public long getPendingRegistrationsCount() {
        return registrationRequestRepository.countByStatus("PENDING");
    }
    
    @ModelAttribute("loginAttemptService")
    public LoginAttemptService getLoginAttemptService() {
        return loginAttemptService;
    }
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @GetMapping("/login")
    public String login() {
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
            !AnonymousAuthenticationToken.class.isAssignableFrom(SecurityContextHolder.getContext().getAuthentication().getClass())) {
            return "redirect:/";
        }
        return "login";
    }
    
    @GetMapping("/users")
    public String listUsers(Model model, Principal principal) {
        // 检查是否为管理员
        User currentUser = userService.findByUsername(principal.getName());
        boolean isAdmin = currentUser.getRole() != null && 
                         (currentUser.getRole().equals("ROLE_ADMIN") || 
                          currentUser.getRole().equals("ADMIN"));
        
        if (!isAdmin) {
            return "redirect:/";  // 非管理员重定向到首页
        }
        
        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "users";
    }
    
    @GetMapping("/edit/{username}")
    public String editUser(@PathVariable String username, Model model, Principal principal) {
        if (!hasEditPermission(principal.getName(), username)) {
            return "redirect:/users?error=unauthorized";
        }
        
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);
        return "edit";
    }
    
    @PostMapping("/update")
    public String updateUser(@ModelAttribute User user, Principal principal) {
        if (!hasEditPermission(principal.getName(), user.getUsername())) {
            return "redirect:/users?error=unauthorized";
        }
        
        User existingUser = userService.findByUsername(user.getUsername());
        existingUser.setUrl(user.getUrl());
        userService.saveUser(existingUser);
        
        // 判断当前用户是否为管理员
        User currentUser = userService.findByUsername(principal.getName());
        boolean isAdmin = currentUser.getRole() != null && 
                         (currentUser.getRole().equals("ROLE_ADMIN") || 
                          currentUser.getRole().equals("ADMIN"));
        
        // 如果是管理员，重定向到用户管理页面；如果是普通用户，重定向到首页
        return isAdmin ? "redirect:/users" : "redirect:/";
    }
    
    private boolean hasEditPermission(String currentUsername, String targetUsername) {
        User currentUser = userService.findByUsername(currentUsername);
        // 检查用户是否是管理员
        boolean isAdmin = currentUser.getRole() != null && 
                        (currentUser.getRole().equals("ROLE_ADMIN") || 
                         currentUser.getRole().equals("ADMIN"));
        return isAdmin || currentUsername.equals(targetUsername);
    }
}

