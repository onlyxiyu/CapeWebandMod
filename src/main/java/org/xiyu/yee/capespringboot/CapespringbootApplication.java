package org.xiyu.yee.capespringboot;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.ApplicationContext;
import org.xiyu.yee.capespringboot.service.UserService;

import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EntityScan("org.xiyu.yee.capespringboot.model")
@EnableJpaRepositories("org.xiyu.yee.capespringboot.repository")
public class CapespringbootApplication {

    @Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(CapespringbootApplication.class, args);
        
        // 测试密码加密
        UserService userService = context.getBean(UserService.class);
        System.out.println("Encoded password for 'password': " + userService.generateEncodedPassword("password"));
    }

}
