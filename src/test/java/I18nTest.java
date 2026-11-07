import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class I18nTest {

    private static final String[] REQUIRED_KEYS = {
            "app.title",
            "language.content",
            "tab.temperature",
            "tab.speed",
            "label.value",
            "label.from",
            "label.to",
            "label.distance",
            "label.time",
            "label.speedUnit",
            "button.convert",
            "button.calculateSpeed",
            "label.result",
            "prompt.enterValue",
            "prompt.enterDistance",
            "prompt.enterTime",
            "history.title",
            "history.empty",
            "speed.history.title",
            "speed.history.empty",
            "error.invalidInput",
            "error.selectUnits",
            "error.invalidTime",
            "error.database",
            "result.format",
            "speed.result.format"
    };

    @AfterEach
    void resetLanguage() {
        I18n.setLocale(Locale.US);
    }

    @ParameterizedTest
    @EnumSource(Language.class)
    @DisplayName("Every supported language bundle contains all UI keys")
    void everyBundleContainsRequiredKeys(Language language) {
        ResourceBundle bundle = I18n.load(language.getLocale());

        assertEquals(language.getLocale(), bundle.getLocale());

        for (String key : REQUIRED_KEYS) {
            assertTrue(bundle.containsKey(key), () -> "Missing key: " + key);
            assertFalse(bundle.getString(key).isBlank(), () -> "Blank value: " + key);
        }
    }

    @Test
    @DisplayName("All bundles contain the same keys")
    void allBundlesHaveTheSameKeys() {
        ResourceBundle base = I18n.load(Locale.US);

        for (Language language : Language.values()) {
            assertEquals(
                    base.keySet(),
                    I18n.load(language.getLocale()).keySet(),
                    "Key mismatch for " + language
            );
        }
    }

    @Test
    @DisplayName("English is the default language")
    void englishIsDefault() {
        ResourceBundle bundle = I18n.load(Locale.US);

        assertEquals("Temperature Converter", bundle.getString("app.title"));
        assertEquals("Convert", bundle.getString("button.convert"));
    }

    @Test
    @DisplayName("Finnish, Swedish, Japanese and Arabic bundles are localized")
    void bundlesAreLocalized() {
        assertEquals("Lämpötilamuunnin",
                I18n.load(Language.FINNISH.getLocale()).getString("app.title"));
        assertEquals("Temperaturomvandlare",
                I18n.load(Language.SWEDISH.getLocale()).getString("app.title"));
        assertEquals("温度変換",
                I18n.load(Language.JAPANESE.getLocale()).getString("app.title"));
        assertEquals("محول درجة الحرارة",
                I18n.load(Language.ARABIC.getLocale()).getString("app.title"));
    }

    @Test
    @DisplayName("Japanese properties are read as UTF-8, not ISO-8859-1")
    void japaneseBundleUsesUtf8() {
        ResourceBundle bundle = I18n.load(Locale.JAPAN);

        String title = bundle.getString("app.title");
        assertEquals("温度変換", title);
        assertTrue(title.codePoints().anyMatch(codePoint -> codePoint > 0x7F));

        String error = bundle.getString("error.invalidInput");
        assertEquals("無効な入力です!", error);
    }

    @Test
    @DisplayName("Arabic properties are read as UTF-8")
    void arabicBundleUsesUtf8() {
        ResourceBundle bundle = I18n.load(Language.ARABIC.getLocale());

        String title = bundle.getString("app.title");
        assertEquals("محول درجة الحرارة", title);
        assertTrue(title.codePoints().anyMatch(codePoint -> codePoint > 0x7F));
    }

    @Test
    @DisplayName("Language codes are resolved case-insensitively")
    void languageCodesAreResolved() {
        assertEquals(Language.ENGLISH, Language.fromCode("en"));
        assertEquals(Language.ENGLISH, Language.fromCode("en_US"));
        assertEquals(Language.FINNISH, Language.fromCode("fi-FI"));
        assertEquals(Language.SWEDISH, Language.fromCode("sv_SE"));
        assertEquals(Language.JAPANESE, Language.fromCode("ja_JP"));
        assertEquals(Language.JAPANESE, Language.fromCode("JA"));
        assertEquals(Language.ARABIC, Language.fromCode("ar_SA"));
        assertEquals(Language.ARABIC, Language.fromCode("ar-SA"));
        assertEquals(Language.ARABIC, Language.fromCode("ar"));
    }

    @Test
    @DisplayName("Unsupported language codes return null")
    void unsupportedLanguageCodesReturnNull() {
        assertNull(Language.fromCode("de_DE"));
        assertNull(Language.fromCode(" "));
        assertNull(Language.fromCode(null));
    }

    @Test
    @DisplayName("Language display names are shown in their own language")
    void languageDisplayNamesAreLocalized() {
        assertEquals("English", Language.ENGLISH.getDisplayName());
        assertEquals("Suomi", Language.FINNISH.getDisplayName());
        assertEquals("Svenska", Language.SWEDISH.getDisplayName());
        assertEquals("日本語", Language.JAPANESE.toString());
        assertEquals("العربية", Language.ARABIC.getDisplayName());
    }

    @Test
    @DisplayName("The active bundle exposes keys and its locale")
    void activeBundleExposesKeysAndLocale() {
        I18n.setLocale(Language.JAPANESE.getLocale());

        assertTrue(I18n.contains("app.title"));
        assertFalse(I18n.contains("missing.key"));
        assertEquals(Language.JAPANESE.getLocale(), I18n.getLocale());
        assertEquals("温度変換", I18n.get("app.title"));
    }

    @Test
    @DisplayName("Formatted results use the active locale")
    void formattedResultsUseActiveLocale() {
        I18n.setLocale(Locale.US);
        assertEquals("Result: 20.00 Celsius",
                I18n.format("result.format", 20.0, "Celsius"));

        I18n.setLocale(Language.FINNISH.getLocale());
        assertEquals("Tulos: 20,00 Celsius",
                I18n.format("result.format", 20.0, "Celsius"));
    }

    @Test
    @DisplayName("The static bundle is available before the UI starts")
    void defaultBundleIsAvailable() {
        assertNotNull(I18n.get("app.title"));
        assertEquals("Temperature Converter", I18n.get("app.title"));
    }
}
