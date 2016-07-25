package su.asgor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import su.asgor.dao.UserRepository;
import su.asgor.model.User;

import javax.sql.DataSource;
import java.util.Collections;

@Service("userDetailsServiceImpl")
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    DataSource dataSource;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null)
            throw new UsernameNotFoundException("username " + username + " not found");

        return new org.springframework.security.core.userdetails.User(user.getEmail(),user.getPassword(),
                user.getEnabled(), true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
