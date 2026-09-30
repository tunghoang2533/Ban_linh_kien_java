package com.banlinhkien.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String avatar;

    @Column(length = 20)
    private String role;

    @Column(name = "is_admin")
    private Boolean isAdmin;

    @Column(name = "is_blocked")
    private Boolean isBlocked;

    @Column(name = "blocked_reason")
    private String blockedReason;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.role == null) {
            this.role = "user";
        }
        if (this.isAdmin == null) {
            this.isAdmin = "admin".equalsIgnoreCase(this.role);
        }
        if (this.isBlocked == null) {
            this.isBlocked = false;
        }
    }

    public Boolean getIsActive() {
        return !Boolean.TRUE.equals(this.isBlocked);
    }

    public void setIsActive(Boolean active) {
        this.isBlocked = !Boolean.TRUE.equals(active);
    }

    public String getPasswordHash() {
        return this.password;
    }

    public void setPasswordHash(String hash) {
        this.password = hash;
    }
}

