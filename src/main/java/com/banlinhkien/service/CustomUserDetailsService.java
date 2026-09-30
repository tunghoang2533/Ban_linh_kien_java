package com.banlinhkien.service;

import com.banlinhkien.entity.User;
import com.banlinhkien.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(login)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản hoặc email không tồn tại: " + login));

        if (Boolean.TRUE.equals(user.getIsBlocked())) {
            throw new UsernameNotFoundException("Tài khoản đã bị tạm khóa: " + 
                    (user.getBlockedReason() != null ? user.getBlockedReason() : "Vui lòng liên hệ quản trị viên"));
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        String roleName = user.getRole() != null ? user.getRole().toUpperCase() : "USER";
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));

        if (Boolean.TRUE.equals(user.getIsAdmin()) && !roleName.equals("ADMIN")) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
