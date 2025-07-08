package com.resume.generator.config;

import com.resume.generator.entity.Role;
import com.resume.generator.entity.User;
import com.resume.generator.repository.RoleRepository;
import com.resume.generator.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

        if (userRepository.findByUsername("xnrHyper").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("xnrHyper");
            adminUser.setPasswordHash(passwordEncoder.encode("1010411661"));
            adminUser.setEmail("admin@hyper.com"); // Use a unique email for the admin
            adminUser.setRoles(Set.of(userRole, adminRole));
            userRepository.save(adminUser);
        }
    }
}