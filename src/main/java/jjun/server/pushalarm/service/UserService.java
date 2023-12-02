package jjun.server.pushalarm.service;

import jjun.server.pushalarm.dto.response.GetUserResponse;
import jjun.server.pushalarm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public GetUserResponse getUserById(Long userId) {
        return GetUserResponse.of(userRepository.findById(userId).get());
    }
}
