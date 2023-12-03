package jjun.server.pushalarm.dto.fcm;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FCMPushRequestDto {

    private String targetToken;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String title;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String body;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String image;

    public static FCMPushRequestDto sendTestPush(String targetToken) {

        return FCMPushRequestDto.builder()
                .targetToken(targetToken)
                .title("💚DO SOPT SERVER💚")
                .body("서팟 앱잼 화이팅! FCM 화이팅!")
                .build();
    }
}