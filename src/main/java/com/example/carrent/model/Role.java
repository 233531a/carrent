package com.example.carrent.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_roles_name", columnNames = "name"))
public class Role {

    public static final String ROLE_CLIENT   = "ROLE_CLIENT";
    public static final String ROLE_EMPLOYEE = "ROLE_EMPLOYEE";
    public static final String ROLE_MANAGER  = "ROLE_MANAGER";
    public static final String ROLE_ADMIN    = "ROLE_ADMIN";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 'ROLE_CLIENT', 'ROLE_EMPLOYEE', ...

    public Role() {}
    public Role(String name) { this.name = name; }

    public Long getId() { return id; }
    public String getName() { return name; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
}
