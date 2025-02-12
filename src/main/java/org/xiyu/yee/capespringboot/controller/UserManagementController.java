package org.xiyu.yee.capespringboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.xiyu.yee.capespringboot.service.UserService;
import org.xiyu.yee.capespringboot.model.User;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UserManagementController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/users/delete/{username}")
    public String deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return "redirect:/users";
    }
    
    @PostMapping("/users/schedule-delete/{username}")
    public String scheduleDeleteUser(@PathVariable String username, @RequestParam int minutes) {
        userService.scheduleUserDeletion(username, minutes);
        return "redirect:/users";
    }
    
    @PostMapping("/users/add")
    public String addUser(@ModelAttribute User user) {
        userService.createUser(user);
        return "redirect:/users";
    }
    
    @PostMapping("/users/cancel-delete/{username}")
    public String cancelScheduledDeletion(@PathVariable String username) {
        userService.cancelScheduledDeletion(username);
        return "redirect:/users";
    }
} 