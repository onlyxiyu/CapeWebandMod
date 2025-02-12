package org.xiyu.yee.capespringboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {
    
    @RequestMapping("/error")
    public String handleError(Model model) {
        model.addAttribute("message", "发生了一个错误，请稍后重试");
        return "error";
    }
} 