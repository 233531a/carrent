package com.example.carrent.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=100)
    private String username;

    @Column(nullable=false, length=255)
    private String password;

    @Column(nullable=false)
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public User() {}

    // --- Getters / Setters ---
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean isEnabled() { return enabled; }
    public Set<Role> getRoles() { return roles; }

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    // --- Удобные методы ---
    public void addRole(Role role) {
        this.roles.add(role);
    }

    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(r -> r.getName().equalsIgnoreCase(roleName));
    }
}
