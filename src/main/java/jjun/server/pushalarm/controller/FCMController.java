package jjun.server.pushalarm.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jjun.server.pushalarm.domain.User;
import jjun.server.pushalarm.dto.fcm.FCMPushRequestDto;
import jjun.server.pushalarm.service.FCMService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
public class FCMController {

    private final FCMService fcmService;

    /**
     * 헤더와 바디를 직접 만들어 알림을 전송하는 테스트용 API (상대 답변 알람 전송에 사용)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> sendNotificationByToken(@RequestBody FCMPushRequestDto request) throws IOException {

        fcmService.pushAlarm(request);
        return ResponseEntity.ok().body("푸시알림 전송에 성공했습니다!");
    }

    /**
     * 동시에 여러 사람에게 푸시 알림을 보내보는 테스트용 API (주기적 알람 전송에 사용)
     */
    @PostMapping("/parentchild")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> sendMultiScheduledTest(@RequestBody FCMPushRequestDto request) {
        List<User> userList = new ArrayList<>();
        fcmService.multipleSendByToken(FCMPushRequestDto.sendTestPush(request.getTargetToken()), userList);
        return ResponseEntity.ok().body("푸시알림 전송에 성공했습니다!");
    }
}
