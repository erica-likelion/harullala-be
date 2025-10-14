package likelion.harullala.service;

import likelion.harullala.domain.Character;

import java.util.List;

public interface CharacterService {
    List<Character> getCharacterList();
    void selectCharacter(Long userId, Long characterId);
}
