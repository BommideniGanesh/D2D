package com.example.demo.authorization;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createRoleIfNotFound("USER", "Customer using delivery service");
        createRoleIfNotFound("DRIVER", "Delivery driver");
        createRoleIfNotFound("ADMIN", "System administrator");
        createRoleIfNotFound("SUPPORT", "Customer support agent");
    }

    private void createRoleIfNotFound(String roleName, String description) {
        if (roleRepository.findByRoleName(roleName).isEmpty()) {
            Role role = Role.builder()
                    .roleName(roleName)
                    .description(description)
                    .build();
            roleRepository.save(role);
        }
    }
}
