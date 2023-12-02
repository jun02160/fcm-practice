package jjun.server.pushalarm.controller;

import jjun.server.pushalarm.dto.response.GetUserResponse;
import jjun.server.pushalarm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<GetUserResponse> findUser(@PathVariable Long userId) {
        return ResponseEntity.ok().body(userService.getUserById(userId));
    }
}
