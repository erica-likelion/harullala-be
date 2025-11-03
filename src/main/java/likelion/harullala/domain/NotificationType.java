package likelion.harullala.domain;

/**
 * 알림 타입 정의
 */
public enum NotificationType {
    AI_FEEDBACK("AI 피드백"),
    FRIEND_REQUEST("친구 요청"),
    FRIEND_ACCEPTED("친구 수락");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

