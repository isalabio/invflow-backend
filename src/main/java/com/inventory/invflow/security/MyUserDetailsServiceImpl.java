package com.inventory.invflow.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.inventory.invflow.entity.User;
import com.inventory.invflow.repository.UserRepository;

@Service
public class MyUserDetailsServiceImpl implements UserDetailsService{

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        //從資料庫中查詢 user 數據 
        User user = userRepository.findByUserName(username)
            .orElseThrow(() -> new UsernameNotFoundException("查無： " + username));

        return new MyUserDetails(user);
    }
    
}
