package likelion.harullala.infra;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import likelion.harullala.domain.AiCharacter;
import likelion.harullala.domain.EmojiEmotion;

@Component
@RequiredArgsConstructor
public class RecordReader {
    private final JdbcTemplate jdbc;

    public record RecRow(Long recordId, Long userId, String text, EmojiEmotion emoji, AiCharacter character) {}

    public RecRow findActiveRecord(Long recordId) {
        return jdbc.query("""
                select r.record_id, r.user_id, r.record, r.emoji_emotion, c.name
                from emotion_record r
                join users u on r.user_id = u.user_id
                join user_characters uc on u.user_id = uc.user_id
                join characters c on uc.character_id = c.id
                where r.record_id = ? and r.is_deleted = false and uc.is_active = true""",
                ps -> ps.setLong(1, recordId),
                rs -> rs.next()
                        ? new RecRow(rs.getLong(1), rs.getLong(2), rs.getString(3), 
                                   EmojiEmotion.valueOf(rs.getString(4)), 
                                   AiCharacter.valueOf(rs.getString(5)))
                        : null
        );
    }
}