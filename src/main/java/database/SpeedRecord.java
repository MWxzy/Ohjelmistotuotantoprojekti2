package database;

import java.time.LocalDateTime;

public record SpeedRecord(
        long id,
        double distanceValue,
        String distanceName,
        String distanceSymbol,
        double timeValue,
        String timeName,
        String timeSymbol,
        double speedValue,
        String speedName,
        String speedSymbol,
        String languageCode,
        LocalDateTime createdAt
) {
}
