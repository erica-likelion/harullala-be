package likelion.harullala.infra;

import org.springframework.stereotype.Component;

import likelion.harullala.domain.Character;
import likelion.harullala.domain.EmotionRecord;
import likelion.harullala.domain.UserCharacter;
import likelion.harullala.repository.EmotionRecordRepository;
import likelion.harullala.repository.UserCharacterRepository;
import likelion.harullala.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecordReader {
    private final EmotionRecordRepository emotionRecordRepository;
    private final UserCharacterRepository userCharacterRepository;
    private final EncryptionUtil encryptionUtil;

    public record RecRow(Long recordId, Long userId, String text, Character character) {}

    public RecRow findActiveRecord(Long recordId) {
        // 1. 감정 기록 조회
        EmotionRecord record = emotionRecordRepository.findById(recordId)
                .orElse(null);
        
        if (record == null) {
            return null;
        }
        
        // 2. 사용자 ID로 현재 선택한 캐릭터 조회 (인사말 조회와 동일한 방식)
        UserCharacter userCharacter = userCharacterRepository.findByUserId(record.getUserId())
                .orElse(null);
        
        if (userCharacter == null) {
            return null;
        }
        
        Character character = userCharacter.getSelectedCharacter();
        
        String decryptedRecord = encryptionUtil.decrypt(record.getRecord());

        return new RecRow(
                record.getRecordId(),
                record.getUserId(),
                decryptedRecord,
                character
        );
    }
}