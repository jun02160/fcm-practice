package jjun.server.pushalarm.common.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public enum ErrorType {

    /**
     * 400 BAD REQUEST
     */

    /**
     * 401 UNAUTHORIZED
     */
    INVALID_FIREBASE_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 파이어베이스 토큰입니다."),


    /**
     * 404 NOT FOUND
     */


    /**
     * 500 INTERNAL SERVER ERROR
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 서버 에러가 발생했습니다"),
    FIREBASE_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파이어베이스 서버와의 연결에 실패했습니다."),
    FAIL_TO_SEND_PUSH_ALARM(HttpStatus.INTERNAL_SERVER_ERROR, "푸시 알림 메세지 전송에 실패했습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getHttpStatusCode() {
        return httpStatus.value();
    }

}
