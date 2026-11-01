import java.util.Locale;

public enum Language {
    ENGLISH(Locale.US, "English"),
    FINNISH(Locale.forLanguageTag("fi-FI"), "Suomi"),
    SWEDISH(Locale.forLanguageTag("sv-SE"), "Svenska"),
    JAPANESE(Locale.JAPAN, "日本語"),
    ARABIC(Locale.forLanguageTag("ar-SA"), "العربية");

    private final Locale locale;
    private final String displayName;

    Language(Locale locale, String displayName) {
        this.locale = locale;
        this.displayName = displayName;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Language fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        String normalized = code.trim().replace('_', '-').toLowerCase(Locale.ROOT);
        for (Language language : values()) {
            String tag = language.locale.toLanguageTag().toLowerCase(Locale.ROOT);
            if (tag.equals(normalized) || language.locale.getLanguage().equals(normalized)) {
                return language;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
