package com.example.carrent.controller;

import com.example.carrent.model.Role;
import com.example.carrent.model.User;
import com.example.carrent.repository.RoleRepository;
import com.example.carrent.repository.UserRepository;
import com.example.carrent.service.AdminUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Контроллер для управления пользователями администратором.
 *
 * Предоставляет функции:
 * - Просмотр списка всех пользователей с поиском
 * - Изменение ролей пользователей
 * - Удаление пользователей (с каскадным удалением связанных данных)
 *
 * Доступен только пользователям с ролью ADMIN.
 *
 * @author Система аренды автомобилей
 * @version 1.0
 * @since 2025-01-25
 */
@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UsersAdminController {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final AdminUserService adminUserService;

    public UsersAdminController(UserRepository userRepo,
                                RoleRepository roleRepo,
                                AdminUserService adminUserService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String query, Model model) {
        model.addAttribute("title", "Управление пользователями");
        
        List<User> users;
        if (query != null && !query.trim().isEmpty()) {
            String searchQuery = query.trim().toLowerCase();
            users = userRepo.findAll().stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(searchQuery))
                    .toList();
            model.addAttribute("q", query);
        } else {
            users = userRepo.findAll();
        }
        
        model.addAttribute("users", users);
        model.addAttribute("allRoles", roleRepo.findAll());
        return "users"; // templates/users.html
    }

    @PostMapping("/{id}/roles")
    public String updateRoles(@PathVariable Long id,
                              @RequestParam(value = "role", required = false) List<String> roles,
                              RedirectAttributes ra) {
        User u = userRepo.findById(id).orElseThrow();
        Set<Role> newRoles = new HashSet<>();
        if (roles != null) {
            for (String r : roles) {
                Role role = roleRepo.findByName(r).orElseGet(() -> roleRepo.save(new Role(r)));
                newRoles.add(role);
            }
        }
        u.setRoles(newRoles);
        userRepo.save(u);
        ra.addFlashAttribute("msg", "Роли пользователя обновлены (ID: " + id + ")");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        adminUserService.deleteUserHard(id);
        ra.addFlashAttribute("msg", "Пользователь удалён (ID: " + id + ")");
        return "redirect:/admin/users";
    }
}
