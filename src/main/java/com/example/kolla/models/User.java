package com.example.kolla.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.kolla.enums.Degree;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "user")
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;
    @Column(name = "password")
    private String password;
    @Column(name = "user_code", columnDefinition = "VARCHAR(100)")
    private String userCode;
    @Column(name = "is_active", columnDefinition = "TINYINT(1)")
    private boolean isActive;
    @Column(name = "dob", columnDefinition = "DATE")
    private LocalDate dob;
    @Enumerated(EnumType.STRING)
    private Degree degree;
    @Column(name = "phone_number")
    private String phoneNumber;
    @Column(name = "identification")
    private String identification;
    @Column(name = "address")
    private String address;
    @Column(name = "bank_name")
    private String bankName;
    @Column(name = "bank_number")
    private String bankNumber;
    @Column(name = "img_url", columnDefinition = "TEXT")
    private String imgUrl;
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role != null 
            ? List.of(new SimpleGrantedAuthority("ROLE_" + role.getName()))
            : List.of();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }

    @Override
    public String getPassword() {
        return this.password;
    }
}