package rangerclickhousesync.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import rangerclickhousesync.dto.RangerPolicyDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.MessageDigest;

/**
 * Утилитарный класс для вычисления SHA-256 хеша политики Ranger.
 * <p>
 * Используется для сравнения текущего состояния политики с сохранённым в БД,
 * чтобы определить необходимость её повторного применения в ClickHouse.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HashUtils {

    /**
     * Вычисляет SHA-256 хеш значимых полей политики.
     *
     * @param policy политика Ranger
     * @return 64-символьная строка с шестнадцатеричным представлением хеша
     * @throws RuntimeException если не удалось сериализовать политику или вычислить хеш
     */
    public static String computeContentHash(final RangerPolicyDto policy) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final String json = mapper.writeValueAsString(policy);
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] hash = md.digest(json.getBytes());
            final StringBuilder hexString = new StringBuilder();
            for (final byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute hash", e);
        }
    }
}
