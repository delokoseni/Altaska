package com.example.Altaska.services;

import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.UsersRepository;
import com.example.Altaska.security.CustomUserDetails;
import java.util.ArrayList;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Users> user = this.usersRepository.findByEmail(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        } else {
            return new CustomUserDetails((user.get()).getEmail(), (user.get()).getPassword(), new ArrayList<>(), (user.get()).getId());
        }
    }
}
