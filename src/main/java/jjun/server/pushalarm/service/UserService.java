package jjun.server.pushalarm.service;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import jjun.server.pushalarm.domain.User;
import jjun.server.pushalarm.dto.fcm.FCMPushRequestDto;
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

    // 푸시알림 활성화가 필요한 로직이라면, FCMService를 주입!
    private final FCMService fcmService;

    public GetUserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 회원의 아이디입니다.")
        );
        //== User 조회 API 호출 시 푸시 알림 전송! ==//
        fcmService.pushAlarm(FCMPushRequestDto.sendTestPush(user.getFcmToken()));
        return GetUserResponse.of(user);
    }
}
