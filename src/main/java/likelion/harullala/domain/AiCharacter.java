package likelion.harullala.domain;

public enum AiCharacter {
    T("T", "논리적이고 분석적인 캐릭터"),
    F("F", "감정적이고 공감적인 캐릭터"),
    EMOTIONAL("감정풍부", "감정을 풍부하게 표현하는 캐릭터"),
    COOL("냉철", "냉철하고 객관적인 캐릭터");
    
    private final String code;
    private final String description;
    
    AiCharacter(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() { return code; }
    public String getDescription() { return description; }
}