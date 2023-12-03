package jjun.server.pushalarm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.TopicManagementResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import jjun.server.pushalarm.common.exception.ErrorType;
import jjun.server.pushalarm.domain.User;
import jjun.server.pushalarm.dto.fcm.FCMMessage;
import jjun.server.pushalarm.dto.fcm.FCMPushRequestDto;
import jjun.server.pushalarm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final ObjectMapper objectMapper;  // FCM의 body 형태에 따라 생성한 값을 문자열로 저장하기 위한 Mapper 클래스

    @Value("${fcm.key.path}")
    private String SERVICE_ACCOUNT_JSON;
    @Value("${fcm.api.url}")
    private String FCM_API_URL;
    @Value("${fcm.topic}")
    private String topic;

    /**
     * 단일 기기
     * - Firebase에 메시지를 수신하는 함수 (헤더와 바디 직접 만들기)
     */
    @Transactional
    public String pushAlarm(FCMPushRequestDto request) {

        String message = makeSingleMessage(request);
        sendPushMessage(message);
        return "알림을 성공적으로 전송했습니다. targetUserId = " + request.getTargetToken();
    }

    /**
     * 다수 기기
     * - Firebase에 메시지를 수신하는 함수 (동일한 메시지를 2명 이상의 유저에게 발송)
     */
    public String multipleSendByToken(FCMPushRequestDto request, List<User> userList) {

        // User 리스트에서 FCM 토큰만 꺼내와서 리스트로 저장
        List<String> tokenList = userList.stream()
                .map(User::getFcmToken).toList();

        // 2명만 있다고 가정
        log.info("tokenList: {}🌈,  {}🌈",tokenList.get(0), tokenList.get(1));

        MulticastMessage message = makeMultipleMessage(request, tokenList);

        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            log.info("다수 기기 알림 전송 성공 ! successCount: " + response.getSuccessCount() + " messages were sent successfully");
            log.info("알림 전송: {}", response.getResponses().toString());

            return "알림을 성공적으로 전송했습니다. \ntargetUserId = 1." + tokenList.get(0) + ", \n\n2." + tokenList.get(1);
        } catch (FirebaseMessagingException e) {
            log.error("다수기기 푸시메시지 전송 실패 - FirebaseMessagingException: {}", e.getMessage());
            throw new IllegalArgumentException(ErrorType.FAIL_TO_SEND_PUSH_ALARM.getMessage());
        }
    }

    /**
     * 주제 구독 등록 및 취소
     * - 특정 타깃 토큰 없이 해당 주제를 구독한 모든 유저에 푸시 전송
     */
    @Transactional
    public String pushTopicAlarm(FCMPushRequestDto request) {


        String message = makeTopicMessage(request);
        sendPushMessage(message);
        return "알림을 성공적으로 전송했습니다. targetUserId = " + request.getTargetToken();
    }

    // Topic 구독 설정 - application.yml에서 topic명 관리
    // 단일 요청으로 최대 1000개의 기기를 Topic에 구독 등록 및 취소할 수 있다.

    public void subscribe() throws FirebaseMessagingException {
        // These registration tokens come from the client FCM SDKs.
        List<String> registrationTokens = Arrays.asList(
                "YOUR_REGISTRATION_TOKEN_1",
                // ...
                "YOUR_REGISTRATION_TOKEN_n"
        );

        // Subscribe the devices corresponding to the registration tokens to the topic.
        TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(
                registrationTokens, topic);

        log.info(response.getSuccessCount() + " tokens were subscribed successfully");
    }

    // Topic 구독 취소
    public void unsubscribe() throws FirebaseMessagingException {
        // These registration tokens come from the client FCM SDKs.
        List<String> registrationTokens = Arrays.asList(
                "YOUR_REGISTRATION_TOKEN_1",
                // ...
                "YOUR_REGISTRATION_TOKEN_n"
        );

        // Unsubscribe the devices corresponding to the registration tokens from the topic.
        TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(
                registrationTokens, topic);

        log.info(response.getSuccessCount() + " tokens were unsubscribed successfully");
    }

    // 요청 파라미터를 FCM의 body 형태로 만들어주는 메서드 [단일 기기]

    private String makeSingleMessage(FCMPushRequestDto request) {

        try {
            FCMMessage fcmMessage = FCMMessage.builder()
                    .message(FCMMessage.Message.builder()
                            .token(request.getTargetToken())   // 1:1 전송 시 반드시 필요한 대상 토큰 설정
                            .notification(FCMMessage.Notification.builder()
                                    .title(request.getTitle())
                                    .body(request.getBody())
                                    .image(request.getImage())
                                    .build())
                            .build()
                    ).validateOnly(false)
                    .build();

            return objectMapper.writeValueAsString(fcmMessage);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 처리 도중에 예외가 발생했습니다.");
        }
    }

    // 요청 파라미터를 FCM의 body 형태로 만들어주는 메서드 [주제 구독]
    private String makeTopicMessage(FCMPushRequestDto request) {

        try {
            FCMMessage fcmMessage = FCMMessage.builder()
                    .message(FCMMessage.Message.builder()
                                    .topic(topic)   // 토픽 구독에서 반드시 필요한 설정 (token 지정 x)
                                    .notification(FCMMessage.Notification.builder()
                                            .title(request.getTitle())
                                            .body(request.getBody())
                                            .image(request.getImage())
                                            .build())
                                    .build()
                    ).validateOnly(false)
                    .build();

            return objectMapper.writeValueAsString(fcmMessage);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 처리 도중에 예외가 발생했습니다.");
        }
    }

    // 요청 파라미터를 FCM의 body 형태로 만들어주는 메서드 [다수 기기]
    private static MulticastMessage makeMultipleMessage(FCMPushRequestDto request, List<String> tokenList) {
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(request.getTitle())
                        .setBody(request.getBody())
                        .setImage(request.getImage())
                        .build())
                .addAllTokens(tokenList)
                .build();

        log.info("message: {}", request.getTitle() +" "+ request.getBody());
        return message;
    }

    // 실제 파이어베이스 서버로 푸시 메시지를 전송하는 메서드
    private void sendPushMessage(String message) {

        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(message, MediaType.get("application/json; charset=utf-8"));
            Request httpRequest = new Request.Builder()
                    .url(FCM_API_URL)
                    .post(requestBody)
                    .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                    .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                    .build();

            Response response = client.newCall(httpRequest).execute();

            log.info("단일 기기 알림 전송 성공 ! successCount: 1 messages were sent successfully");
            log.info("알림 전송: {}", response.body().string());
        } catch (IOException e) {
            throw new IllegalArgumentException("파일을 읽는 데 실패했습니다.");
        }
    }

    // Firebase에서 Access Token 가져오기
    private String getAccessToken() {

        try {

            GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(new ClassPathResource(SERVICE_ACCOUNT_JSON).getInputStream())
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
            googleCredentials.refreshIfExpired();
            log.info("getAccessToken() - googleCredentials: {} ", googleCredentials.getAccessToken().getTokenValue());

            return googleCredentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new IllegalArgumentException("파일을 읽는 데 실패했습니다.");
        }
    }
}
