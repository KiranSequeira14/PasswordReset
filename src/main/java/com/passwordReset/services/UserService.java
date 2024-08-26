package com.passwordReset.services;

import com.passwordReset.DTO.EmailDetails;
import com.passwordReset.DTO.UserDTO;
import com.passwordReset.emailServices.EmailService;
import com.passwordReset.entity.User;
import com.passwordReset.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private Environment env;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public String handleForgotPassword(UserDTO userDTO) {
        String res = this.forgotPassword(userDTO);

        if(res.startsWith("Invalid"))
            return res;

        String resetLink = env.getProperty("password.reset.link") + res;
        //Send the reset link to emailAddress
        String emailBody = "Hey! \n\nHere's the link to reset the password \n\n"+resetLink+" \n\n\n\nThanks";
        String subject = "Password Reset Link";
        emailService.sendSimpleEmail(new EmailDetails(userDTO.getEmail(),emailBody,subject));

        return resetLink;
    }

    private String forgotPassword(UserDTO userDTO) {
        Optional<User> userOptional = Optional.ofNullable(userRepository.findByEmail(userDTO.getEmail()));

        if (!userOptional.isPresent())
            return "Invalid user email Id";

        User user = userOptional.get();
        user.setToken(generateToken());
        user.setTokenCreationDate(LocalDateTime.now());
        userRepository.save(user);

        return user.getToken();
    }

    public String handleResetPassword(String token, UserDTO userDTO) {
        Optional<User> userOptional = Optional.ofNullable(userRepository.findByToken(token));

        if (!userOptional.isPresent()) {
            return "Invalid token";
        }

        User user = userOptional.get();

        LocalDateTime creationTime = user.getTokenCreationDate();
        if (isTokenExpired(creationTime)) {
            return "Token expired";
        }

        user.setPassword(userDTO.getPassword());
        user.setToken(null);
        user.setTokenCreationDate(null);

        userRepository.save(user);
        return "Password reset is successful";
    }

    //Utility function for creating unique token
    private String generateToken() {
        StringBuilder token = new StringBuilder();

        return token.append(UUID.randomUUID().toString())
                .append(UUID.randomUUID().toString()).toString();
    }

    private boolean isTokenExpired(LocalDateTime creationTime) {
        LocalDateTime currentTime = LocalDateTime.now();
        Duration diff = Duration.between(creationTime, currentTime);

        return diff.toMinutes() >= Long.parseLong(env.getProperty("token.expiry.minutes"));
    }
}
