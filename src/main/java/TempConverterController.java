import database.ConversionDAO;
import database.ConversionRecord;
import database.MeasurementDAO;
import database.SpeedRecord;
import database.Unit;
import database.UnitDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TempConverterController {

    private static final DateTimeFormatter HISTORY_TIME =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    private VBox root;

    @FXML
    private ComboBox<Language> languageCombo;

    @FXML
    private TextField valueField;

    @FXML
    private ComboBox<Unit> fromUnit;

    @FXML
    private ComboBox<Unit> toUnit;

    @FXML
    private Label fromDescription;

    @FXML
    private Label toDescription;

    @FXML
    private Label resultLabel;

    @FXML
    private ListView<String> historyList;

    @FXML
    private TextField distanceField;

    @FXML
    private ComboBox<Unit> distanceUnit;

    @FXML
    private Label distanceDescription;

    @FXML
    private TextField timeField;

    @FXML
    private ComboBox<Unit> timeUnit;

    @FXML
    private Label timeDescription;

    @FXML
    private ComboBox<Unit> speedUnit;

    @FXML
    private Label speedDescription;

    @FXML
    private Label speedResultLabel;

    @FXML
    private ListView<String> speedHistoryList;

    private final UnitConverter unitConverter = new UnitConverter();
    private final UnitDAO unitDAO = new UnitDAO();
    private final ConversionDAO conversionDAO = new ConversionDAO();
    private final MeasurementDAO measurementDAO = new MeasurementDAO();

    private TempConverterApp application;
    private Language currentLanguage;
    private boolean languageReady;

    public void setApplication(TempConverterApp application, Language language) {
        this.application = application;
        this.currentLanguage = language;
    }

    @FXML
    private void initialize() {
        if (currentLanguage == null) {
            currentLanguage = Language.ENGLISH;
        }

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

        loadTemperatureUnits();
        loadSpeedUnits();
        refreshHistory();
        refreshSpeedHistory();

        historyList.setPlaceholder(new Label(I18n.get("history.empty")));
        speedHistoryList.setPlaceholder(new Label(I18n.get("speed.history.empty")));
    }

    @FXML
    private void handleConvert() {
        Unit from = fromUnit.getValue();
        Unit to = toUnit.getValue();

        if (from == null || to == null) {
            resultLabel.setText(I18n.get("error.selectUnits"));
            return;
        }

        try {
            double value = parseNumber(valueField.getText());
            double result = unitConverter.convertTemperature(value, from, to);

            resultLabel.setText(I18n.format("result.format", result, to.name()));
            conversionDAO.save(
                    value,
                    from.id(),
                    to.id(),
                    result,
                    languageCode()
            );
            refreshHistory();
        } catch (IllegalArgumentException exception) {
            resultLabel.setText(I18n.get("error.invalidInput"));
        } catch (SQLException exception) {
            resultLabel.setText(I18n.get("error.database"));
        }
    }

    @FXML
    private void handleCalculateSpeed() {
        Unit distance = distanceUnit.getValue();
        Unit time = timeUnit.getValue();
        Unit speed = speedUnit.getValue();

        if (distance == null || time == null || speed == null) {
            speedResultLabel.setText(I18n.get("error.selectUnits"));
            return;
        }

        try {
            double distanceValue = parseNumber(distanceField.getText());
            double timeValue = parseNumber(timeField.getText());

            if (timeValue <= 0) {
                speedResultLabel.setText(I18n.get("error.invalidTime"));
                return;
            }

            double speedValue = unitConverter.calculateSpeed(
                    distanceValue,
                    distance,
                    timeValue,
                    time,
                    speed
            );

            speedResultLabel.setText(
                    I18n.format("speed.result.format", speedValue, speed.name())
            );
            measurementDAO.saveSpeed(
                    distanceValue,
                    distance.id(),
                    timeValue,
                    time.id(),
                    speedValue,
                    speed.id(),
                    languageCode()
            );
            refreshSpeedHistory();
        } catch (IllegalArgumentException exception) {
            speedResultLabel.setText(I18n.get("error.invalidInput"));
        } catch (SQLException exception) {
            speedResultLabel.setText(I18n.get("error.database"));
        }
    }

    private void loadTemperatureUnits() {
        try {
            List<Unit> units = unitDAO.findByCategory(
                    "TEMPERATURE",
                    languageCode()
            );
            setComboItems(fromUnit, units, 0);
            setComboItems(toUnit, units, 1);

            fromUnit.valueProperty().addListener(
                    (observable, oldValue, newValue) -> updateTemperatureDescriptions()
            );
            toUnit.valueProperty().addListener(
                    (observable, oldValue, newValue) -> updateTemperatureDescriptions()
            );
            updateTemperatureDescriptions();
        } catch (SQLException exception) {
            resultLabel.setText(I18n.get("error.database"));
        }
    }

    private void loadSpeedUnits() {
        try {
            List<Unit> distances = unitDAO.findByCategory("DISTANCE", languageCode());
            List<Unit> times = unitDAO.findByCategory("TIME", languageCode());
            List<Unit> speeds = unitDAO.findByCategory("SPEED", languageCode());

            setComboItems(distanceUnit, distances, 1);
            setComboItems(timeUnit, times, 2);
            setComboItems(speedUnit, speeds, 1);

            distanceUnit.valueProperty().addListener(
                    (observable, oldValue, newValue) -> updateSpeedDescriptions()
            );
            timeUnit.valueProperty().addListener(
                    (observable, oldValue, newValue) -> updateSpeedDescriptions()
            );
            speedUnit.valueProperty().addListener(
                    (observable, oldValue, newValue) -> updateSpeedDescriptions()
            );
            updateSpeedDescriptions();
        } catch (SQLException exception) {
            speedResultLabel.setText(I18n.get("error.database"));
        }
    }

    private void setComboItems(ComboBox<Unit> combo, List<Unit> units, int selectedIndex) {
        combo.getItems().setAll(units);
        if (!units.isEmpty()) {
            combo.getSelectionModel().select(
                    Math.min(selectedIndex, units.size() - 1)
            );
        }
    }

    private void updateTemperatureDescriptions() {
        fromDescription.setText(description(fromUnit.getValue()));
        toDescription.setText(description(toUnit.getValue()));
    }

    private void updateSpeedDescriptions() {
        distanceDescription.setText(description(distanceUnit.getValue()));
        timeDescription.setText(description(timeUnit.getValue()));
        speedDescription.setText(description(speedUnit.getValue()));
    }

    private String description(Unit unit) {
        return unit == null || unit.description() == null
                ? ""
                : unit.description();
    }

    private void refreshHistory() {
        try {
            List<ConversionRecord> records =
                    conversionDAO.findRecent(5, languageCode());

            historyList.setItems(FXCollections.observableArrayList(
                    records.stream().map(this::formatHistoryRow).toList()
            ));
        } catch (SQLException exception) {
            resultLabel.setText(I18n.get("error.database"));
        }
    }

    private void refreshSpeedHistory() {
        try {
            List<SpeedRecord> records =
                    measurementDAO.findRecentSpeed(5, languageCode());

            speedHistoryList.setItems(FXCollections.observableArrayList(
                    records.stream().map(this::formatSpeedHistoryRow).toList()
            ));
        } catch (SQLException exception) {
            speedResultLabel.setText(I18n.get("error.database"));
        }
    }

    private String formatHistoryRow(ConversionRecord record) {
        String time = HISTORY_TIME
                .withLocale(currentLanguage.getLocale())
                .format(record.createdAt());

        return String.format(
                currentLanguage.getLocale(),
                "%s | %.2f %s -> %.2f %s",
                time,
                record.inputValue(),
                record.fromName(),
                record.resultValue(),
                record.toName()
        );
    }

    private String formatSpeedHistoryRow(SpeedRecord record) {
        String time = HISTORY_TIME
                .withLocale(currentLanguage.getLocale())
                .format(record.createdAt());

        return String.format(
                currentLanguage.getLocale(),
                "%s | %.2f %s / %.2f %s = %.2f %s",
                time,
                record.distanceValue(),
                record.distanceName(),
                record.timeValue(),
                record.timeName(),
                record.speedValue(),
                record.speedName()
        );
    }

    private String languageCode() {
        return currentLanguage.getLocale().getLanguage();
    }

    private double parseNumber(String text) {
        if (text == null) {
            throw new NumberFormatException();
        }

        String normalized = normalizeDigits(text)
                .replace("\u00A0", "")
                .replace(" ", "")
                .replace("\u066C", "")
                .replace("\u066B", ".")
                .replace(',', '.')
                .trim();

        if (normalized.isEmpty()) {
            throw new NumberFormatException();
        }

        double value = Double.parseDouble(normalized);
        if (!Double.isFinite(value)) {
            throw new NumberFormatException();
        }
        return value;
    }

    private String normalizeDigits(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        for (char character : input.toCharArray()) {
            if (character >= '\u0660' && character <= '\u0669') {
                builder.append((char) ('0' + character - '\u0660'));
            } else if (character >= '\u06F0' && character <= '\u06F9') {
                builder.append((char) ('0' + character - '\u06F0'));
            } else {
                builder.append(character);
            }
        }
        return builder.toString();
    }
}
