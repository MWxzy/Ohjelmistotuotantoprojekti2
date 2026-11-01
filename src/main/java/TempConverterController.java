import database.TempRecordDAO;
import database.TemperatureUnitDAO;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class TempConverterController {

    @FXML
    private VBox root;

    @FXML
    private ComboBox<Language> languageCombo;

    @FXML
    private TextField valueField;

    @FXML
    private ComboBox<String> fromUnit;

    @FXML
    private ComboBox<String> toUnit;

    @FXML
    private Label resultLabel;

    @FXML
    private ResourceBundle resources;

    private final TemperatureConverter converter = new TemperatureConverter();
    private final TemperatureUnitDAO unitDAO = new TemperatureUnitDAO();
    private final TempRecordDAO recordDAO = new TempRecordDAO();

    private TempConverterApp application;
    private Language currentLanguage;
    private boolean languageReady;

    public void setApplication(TempConverterApp application, Language language) {
        this.application = application;
        this.currentLanguage = language;
    }

    @FXML
    private void initialize() {
        root.setNodeOrientation(currentLanguage == Language.ARABIC
                ? NodeOrientation.RIGHT_TO_LEFT
                : NodeOrientation.LEFT_TO_RIGHT);

        languageCombo.getItems().setAll(Language.values());
        languageCombo.setValue(currentLanguage);
        languageCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (languageReady && newValue != null && newValue != currentLanguage) {
                application.switchLanguage(newValue);
            }
        });
        languageReady = true;

        List<String> unitNames = loadUnitNames();
        fromUnit.getItems().setAll(unitNames);
        toUnit.getItems().setAll(unitNames);

        if (!unitNames.isEmpty()) {
            fromUnit.setValue(unitNames.get(0));
            toUnit.setValue(unitNames.size() > 1 ? unitNames.get(1) : unitNames.get(0));
        }
    }

    @FXML
    private void handleConvert() {
        try {
            if (fromUnit.getValue() == null || toUnit.getValue() == null) {
                resultLabel.setText(resources.getString("error.invalidInput"));
                return;
            }

            double value = Double.parseDouble(valueField.getText().trim());
            int fromId = fromUnit.getSelectionModel().getSelectedIndex() + 1;
            int toId = toUnit.getSelectionModel().getSelectedIndex() + 1;
            double result = convert(value, fromId, toId);

            resultLabel.setText(String.format(resources.getLocale(),
                    resources.getString("result.format"), result, toUnit.getValue()));
            recordDAO.saveRecord(value, fromId, toId, result);
        } catch (NumberFormatException exception) {
            resultLabel.setText(resources.getString("error.invalidInput"));
        }
    }

    private List<String> loadUnitNames() {
        List<String> names = unitDAO.getAllUnitNames();
        if (names.size() < 3) {
            names = List.of("Celsius", "Fahrenheit", "Kelvin");
        }
        return names.stream().map(this::localizeUnitName).toList();
    }

    private String localizeUnitName(String name) {
        String key = switch (name.toLowerCase(Locale.ROOT)) {
            case "celsius" -> "unit.celsius";
            case "fahrenheit" -> "unit.fahrenheit";
            case "kelvin" -> "unit.kelvin";
            default -> null;
        };
        return key != null && resources.containsKey(key) ? resources.getString(key) : name;
    }

    private double convert(double value, int fromUnitId, int toUnitId) {
        double celsius = switch (fromUnitId) {
            case 1 -> value;
            case 2 -> converter.fahrenheitToCelsius(value);
            case 3 -> converter.kelvinToCelsius(value);
            default -> value;
        };
        return switch (toUnitId) {
            case 1 -> celsius;
            case 2 -> converter.celsiusToFahrenheit(celsius);
            case 3 -> celsius + 273.15;
            default -> celsius;
        };
    }
}
