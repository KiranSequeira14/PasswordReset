package com.passwordReset.controllers;

import com.passwordReset.DTO.UserDTO;
import com.passwordReset.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestBody UserDTO userDTO) {
        return userService.handleForgotPassword(userDTO);
    }

    @PutMapping("/reset-password")
    public String handleResetPassword(@RequestParam String token, @RequestBody UserDTO userDTO) {
        return userService.handleResetPassword(token, userDTO);
    }
}
