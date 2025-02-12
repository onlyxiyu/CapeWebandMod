package org.xiyu.yee.capespringboot.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Pattern;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @Pattern(regexp = "^[a-zA-Z0-9_\u4e00-\u9fa5]{2,20}$", message = "用户名必须是2-20位的中文、字母、数字或下划线")
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    private String url;
    
    @Column(nullable = false)
    private String role;
    
    @Column(nullable = false)
    private boolean enabled = true;
    
    @Column(name = "delete_at")
    private LocalDateTime deleteAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getDeleteAt() { return deleteAt; }
    public void setDeleteAt(LocalDateTime deleteAt) { this.deleteAt = deleteAt; }
} 