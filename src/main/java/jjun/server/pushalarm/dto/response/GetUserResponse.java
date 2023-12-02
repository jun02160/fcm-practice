package jjun.server.pushalarm.dto.response;

import jjun.server.pushalarm.domain.User;

public record GetUserResponse(
        String nama,
        String fcmToken
) {

    public static GetUserResponse of(User user) {
        return new GetUserResponse(user.getName(), user.getFcmToken());
    }
}
