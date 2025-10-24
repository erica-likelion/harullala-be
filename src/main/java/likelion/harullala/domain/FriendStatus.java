package likelion.harullala.domain;

public enum FriendStatus {
    PENDING,    // 대기중 (친구 요청 보낸 상태)
    ACCEPTED,   // 수락됨 (친구 관계 성립)
    REJECTED,   // 거절됨
    CANCELLED   // 취소됨
}

