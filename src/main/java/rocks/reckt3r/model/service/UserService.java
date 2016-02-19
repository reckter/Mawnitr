package rocks.reckt3r.model.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocks.reckt3r.model.User;
import rocks.reckt3r.model.repository.UserRepository;

/**
 * Created by hannes on 18.02.16.
 */
@Service
public class UserService {


    @Autowired
    UserRepository userRepository;


    public User getOrCreate(long telegramId) {
        User user = userRepository.findOne(telegramId);
        if(user == null) {
            user = new User();
            user.setTelegramId(telegramId);
            user = userRepository.save(user);
        }
        return user;
    }
}
