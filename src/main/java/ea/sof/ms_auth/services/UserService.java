package ea.sof.ms_auth.services;

import ea.sof.ms_auth.entities.User;

public interface UserService {

    User saveUser(User user);
    User findUser(Integer id);
    User findByEmail(String email);
}
