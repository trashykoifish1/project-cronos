package com.timetracker.service;

import com.timetracker.entity.User;
import com.timetracker.exception.ResourceNotFoundException;
import com.timetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Value("${app.mode:local}")
    private String appMode;

    /**
     * Get the current user. In local mode, this returns the default admin user.
     * In hosted mode, this would get the authenticated user from security context.
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        if ("local".equals(appMode)) {
            return getLocalAdminUser();
        } else {
            // TODO: In hosted mode, get user from security context
            // SecurityContext context = SecurityContextHolder.getContext();
            // Authentication auth = context.getAuthentication();
            // return getUserByEmail(auth.getName());
            return getLocalAdminUser(); // Fallback for now
        }
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Get user by email
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /**
     * Get or create the local admin user (for self-hosted mode)
     */
    @Transactional
    public User getLocalAdminUser() {
        return userRepository.findByEmail("admin@localhost")
                .orElseGet(this::createLocalAdminUser);
    }

    /**
     * Create the default local admin user
     */
    private User createLocalAdminUser() {
        User user = new User();
        user.setEmail("admin@localhost");
        user.setName("Local Admin");
        user.setTimeZone("UTC");
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        log.info("Created local admin user: {}", savedUser.getEmail());
        return savedUser;
    }

    /**
     * Update user profile
     */
    public User updateUserProfile(User user, String name, String timeZone) {
        user.setName(name);
        user.setTimeZone(timeZone);

        User savedUser = userRepository.save(user);
        log.info("Updated profile for user: {}", savedUser.getEmail());
        return savedUser;
    }

    /**
     * Check if user exists by email
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}