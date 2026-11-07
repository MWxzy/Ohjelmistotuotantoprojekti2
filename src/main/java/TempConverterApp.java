import database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

public class TempConverterApp extends Application {

    private Stage primaryStage;
    private Language currentLanguage = Language.ENGLISH;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        currentLanguage = resolveInitialLanguage();

        DatabaseManager.initializeDatabase();

        showView(currentLanguage);
        primaryStage.show();
    }

    public void switchLanguage(Language language) {
        currentLanguage = language;
        showView(language);
    }

    private void showView(Language language) {
        try {
            I18n.setLocale(language.getLocale());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/TempConverter.fxml"));
            TempConverterController controller = new TempConverterController();
            controller.setApplication(this, language);
            loader.setController(controller);

            ResourceBundle bundle = I18n.load(language.getLocale());
            loader.setResources(bundle);

            Parent root = loader.load();
            Scene scene = new Scene(root, 760, 760);

            primaryStage.setTitle(bundle.getString("app.title"));
            primaryStage.setMinWidth(700);
            primaryStage.setMinHeight(650);
            primaryStage.setScene(scene);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load the application view", exception);
        }
    }

    private Language resolveInitialLanguage() {
        String requested = System.getProperty("app.locale");
        if (requested == null || requested.isBlank()) {
            requested = System.getenv("APP_LOCALE");
        }
        if (requested == null || requested.isBlank()) {
            for (String argument : getParameters().getRaw()) {
                if (argument.startsWith("--lang=")) {
                    requested = argument.substring("--lang=".length());
                    break;
                }
            }
        }

        if (requested != null && !requested.isBlank()) {
            Language language = Language.fromCode(requested);
            return language != null ? language : Language.ENGLISH;
        }

        return Language.ENGLISH;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
