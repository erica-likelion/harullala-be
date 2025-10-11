package likelion.harullala.controller;

import likelion.harullala.config.security.CustomUserDetails;
import likelion.harullala.dto.ApiSuccess;
import likelion.harullala.dto.CharacterInfo;
import likelion.harullala.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/characters")
public class CharacterController {

    private final CharacterService characterService;

    @GetMapping
    public ApiSuccess<List<CharacterInfo>> getCharacterList() {
        List<CharacterInfo> characterInfos = characterService.getCharacterList().stream()
                .map(CharacterInfo::from)
                .collect(Collectors.toList());
        return ApiSuccess.of(characterInfos, "캐릭터 목록 조회가 성공했습니다.");
    }

    @PostMapping("/{characterId}/select")
    public ApiSuccess<?> selectCharacter(@PathVariable Long characterId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        characterService.selectCharacter(userId, characterId);

        return ApiSuccess.of(null, "캐릭터가 성공적으로 선택되었습니다.");
    }
}
