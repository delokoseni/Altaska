package com.example.Altaska.controller;

import com.example.Altaska.models.Users;
import com.example.Altaska.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/emails")
    public ResponseEntity<Map<Long, String>> getEmailsByIds(@RequestParam List<Long> ids) {
        List<Users> users = usersRepository.findAllById(ids);
        Map<Long, String> result = users.stream()
                .collect(Collectors.toMap(Users::getId, Users::getEmail));
        return ResponseEntity.ok(result);
    }
}
