package com.mycompany.mavenproject1.service;

import com.mycompany.mavenproject1.Role;
import com.mycompany.mavenproject1.model.User;
import com.mycompany.mavenproject1.repository.IRoleRepository;
import com.mycompany.mavenproject1.repository.IUserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IRoleRepository roleRepository;

    public void save(@NotNull User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userRepository.save(user);
    }

    public void setDefaultRole(String username) {
        userRepository.findByUsername(username).ifPresentOrElse(
                user -> {
                    user.getRoles().add(roleRepository.findRoleById(Role.USER.value));
                    userRepository.save(user);
                },
                () -> { throw new UsernameNotFoundException("User not found"); }
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                  .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImpl.build(user);
    }

    public Optional<User> findByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    public long getUserCount() {
        return userRepository.count();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void lockUserAccount(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setAccountNonLocked(false);
            userRepository.save(user);
        });
    }

    public void unlockUserAccount(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setAccountNonLocked(true);
            userRepository.save(user);
        });
    }
}
