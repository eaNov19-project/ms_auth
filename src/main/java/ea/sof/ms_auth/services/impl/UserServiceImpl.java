package ea.sof.ms_auth.services.impl;

import ea.sof.ms_auth.entities.User;
import ea.sof.ms_auth.repositories.UserRepository;
import ea.sof.ms_auth.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PasswordEncoder bcryptEncoder;

    public User saveUser(User user) {
        user.setPassword(bcryptEncoder.encode(user.getPassword()));
        return userRepository.save(user);
}

    public User findUser(Integer id) {
        return userRepository.findById(id).get();
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
