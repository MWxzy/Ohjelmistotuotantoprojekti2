package database;

import java.time.LocalDateTime;

public record ConversionRecord(
        long id,
        double inputValue,
        String fromName,
        String fromSymbol,
        double resultValue,
        String toName,
        String toSymbol,
        String languageCode,
        LocalDateTime createdAt
) {
}
