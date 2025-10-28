package likelion.harullala.infra;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import likelion.harullala.domain.Character;
import likelion.harullala.domain.EmojiEmotion;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecordReader {
    private final JdbcTemplate jdbc;

    public record RecRow(Long recordId, Long userId, String text, EmojiEmotion emoji, Character character) {}

    public RecRow findActiveRecord(Long recordId) {
        return jdbc.query("""
                select r.record_id, r.user_id, r.record, r.emoji_emotion, c.id, c.name, c.description, c.tag, c.image_url
                from emotion_record r
                join users u on r.user_id = u.user_id
                join user_characters uc on u.user_id = uc.user_id
                join characters c on uc.character_id = c.id
                where r.record_id = ? and uc.is_active = true""",
                ps -> ps.setLong(1, recordId),
                rs -> rs.next()
                        ? new RecRow(rs.getLong(1), rs.getLong(2), rs.getString(3), 
                                   EmojiEmotion.valueOf(rs.getString(4)),
                                   new Character(rs.getLong(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9)))
                        : null
        );
    }
}