package likelion.harullala.domain;

/**
 * AI 인사말 컨텍스트 타입
 */
public enum GreetingContext {
    HOME("home", "홈 화면"),
    FRIEND_INVITE("friend-invite", "친구 초대 페이지"),
    FRIEND_REMINDER("friend-reminder", "친구 리마인더");
    
    private final String value;
    private final String description;
    
    GreetingContext(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * String 값으로부터 Enum 변환
     */
    public static GreetingContext fromValue(String value) {
        for (GreetingContext context : values()) {
            if (context.value.equalsIgnoreCase(value)) {
                return context;
            }
        }
        throw new IllegalArgumentException("Invalid context: " + value + 
            ". Valid values are: home, friend-invite, friend-reminder");
    }
}

