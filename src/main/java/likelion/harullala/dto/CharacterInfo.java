package likelion.harullala.dto;

import likelion.harullala.domain.Character;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CharacterInfo {
    private Long id;
    private String name;
    private String tag;
    private String description;
    private String imageUrl;

    public static CharacterInfo from(Character character) {
        return new CharacterInfo(
                character.getId(),
                character.getName(),
                character.getTag(),
                character.getDescription(),
                character.getImageUrl()
        );
    }
}
