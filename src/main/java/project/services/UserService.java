package project.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.models.User;
import project.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save((user));
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<String> getPhotoByUserId(UUID userId) {
        return userRepository.getPhotoPathByUserId(userId);
    }

    @Transactional
    public void saveUserPhoto(String objectKeyS3, UUID userId) {
        userRepository.saveUserPhoto(objectKeyS3, userId);
    }
}
