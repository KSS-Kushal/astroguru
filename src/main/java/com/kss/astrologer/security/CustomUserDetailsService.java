package com.kss.astrologer.security;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kss.astrologer.models.User;
import com.kss.astrologer.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByMobile(username).orElse(null);
        if(user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        // return org.springframework.security.core.userdetails.User.withUsername(username)
        //                         .password(user.getPassword())
        //                         .roles(user.getRole().name()).build();

        return new CustomUserDetails(user.getId(), user.getMobile(), null, user.getRole(), Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name())));
    }
    
}
