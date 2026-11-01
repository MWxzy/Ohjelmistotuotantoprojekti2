# Temperature Converter

## 1. Assignment Description

This project implements a temperature conversion application with a JavaFX graphical user interface. The application allows the user to enter a temperature value, select the original unit, select the target unit, and view the converted result. The interface can be switched between English, Finnish, Swedish, Japanese, and Arabic at runtime.

The supported temperature units are:

- Celsius
- Fahrenheit
- Kelvin

The application also stores conversion records in a local H2 database.

### Main requirements addressed

- Implement a FXML-based JavaFX user interface for temperature conversion.
- Support conversions between Celsius, Fahrenheit, and Kelvin.
- Provide a localized user interface in English, Finnish, Swedish, Japanese, and Arabic.
- Allow the user to change the language dynamically without restarting the application.
- Load the user interface texts from UTF-8 encoded `.properties` resource bundles.
- Use right-to-left layout for Arabic.
- Validate numeric user input.
- Display the converted temperature with two decimal places.
- Store temperature conversion records in a database with two related tables.
- Initialize the database automatically when the application starts.
- Provide automated unit tests for the conversion logic, localization, and database functionality.
- Provide Maven, Docker, Jenkins, and Kubernetes support.

## 2. Technologies & Tools Used

### Programming languages

- Java 17
- HTML
- JavaScript
- CSS
- Groovy, for the Jenkins pipeline

### Libraries and frameworks

- JavaFX 21.0.2
    - `javafx-controls`
    - `javafx-fxml`
    - `javafx-graphics`
    - `javafx-base`
- H2 Database 2.2.224
- JUnit Jupiter 5.13.4
- JaCoCo 0.8.11
- Java `ResourceBundle` / `PropertyResourceBundle` for localization

### Development and build tools

- Apache Maven 3.9 or newer
- Java Development Kit 17
- Docker
- Jenkins
- Minikube and kubectl
- XMing or another X server for running the GUI from a container
- Git and GitHub
- IntelliJ IDEA project configuration

## 3. Design Approach & Implementation Method

### Application structure

The application is divided into four main areas:

1. Graphical user interface
2. Localization
3. Temperature conversion logic
4. Database access

The main application classes are located in `src/main/java`. The localized messages are located in `src/main/resources`.

### Graphical user interface

The GUI is implemented with JavaFX and FXML. The layout is defined in `src/main/resources/TempConverter.fxml`, and the UI logic is handled by `TempConverterController`.

The application window contains:

- A language selector with English, Finnish, Swedish, Japanese, and Arabic
- A text field for entering the temperature value
- A unit selector for the original temperature unit
- A unit selector for the target temperature unit
- A conversion button
- A label for displaying the result or an error message

All labels, prompts, buttons, window titles, unit names, and error messages are loaded from the selected resource bundle. The FXML file uses resource-bundle keys such as `%app.title` and `%button.convert`.

Changing the language reloads the FXML view with the selected `ResourceBundle`. The language change is applied immediately, and no application restart is required. Arabic also switches the scene to right-to-left orientation.

The application starts from the `Launcher` class, which starts `TempConverterApp`.

The main JavaFX application initializes the database before displaying the user interface. The available temperature units are loaded from the database through `TemperatureUnitDAO`.

### Localization

Localization is implemented with Java `ResourceBundle` and `PropertyResourceBundle`. The bundles are always read as UTF-8 by a custom `ResourceBundle.Control`, so the Japanese and Arabic texts do not need Unicode escape sequences.

The project contains the following UTF-8 properties files:

| File | Language |
|---|---|
| `MessagesBundle_en_US.properties` | English (default) |
| `MessagesBundle_fi_FI.properties` | Finnish |
| `MessagesBundle_sv_SE.properties` | Swedish |
| `MessagesBundle_ja_JP.properties` | Japanese |
| `MessagesBundle_ar_SA.properties` | Arabic |
| `MessagesBundle.properties` | English fallback |

The `Language` enum defines the supported locales. The `I18n` class loads the bundle for the selected locale and exposes the localized strings and locale-aware formatting to the UI.

The language selector in the FXML view changes the active `ResourceBundle` at runtime. When Arabic is selected, the scene orientation is set to right-to-left, and the result is formatted with the Arabic locale.

### Temperature conversion

The conversion logic is implemented in the `TemperatureConverter` class. The following conversions are supported:

- Fahrenheit to Celsius
- Celsius to Fahrenheit
- Kelvin to Celsius
- Detection of extreme Celsius temperatures

The GUI supports conversion between all three units by first converting the input value to Celsius and then converting the Celsius value to the selected target unit. The GUI delegates the actual formulas to `TemperatureConverter`.

Temperature values are displayed using two decimal places and the number formatting conventions of the selected locale.

### Database design

The application uses an H2 file-based database stored in the `data` directory.

The database contains two tables:

#### `temperature_unit`

Stores the supported temperature units.

| Column | Description |
|---|---|
| `id` | Unique identifier |
| `name` | Name of the temperature unit |
| `symbol` | Symbol of the temperature unit |

The database is automatically populated with:

- Celsius
- Fahrenheit
- Kelvin

#### `temperature_record`

Stores completed temperature conversions.

| Column | Description |
|---|---|
| `id` | Unique identifier |
| `input_value` | Original temperature value |
| `from_unit_id` | Original temperature unit |
| `to_unit_id` | Target temperature unit |
| `result` | Converted temperature |
| `recorded_at` | Time of the conversion |

The two tables are related through foreign keys: `temperature_record.from_unit_id` and `temperature_record.to_unit_id` both reference `temperature_unit.id`. Every conversion record therefore points to the units used in the conversion.

### Database access

Database operations are separated into DAO classes:

- `TemperatureUnitDAO` retrieves temperature unit names.
- `TempRecordDAO` saves conversion records.
- `DatabaseManager` creates the database connection and initializes the database tables.

Prepared statements are used when storing conversion records.

### Build and deployment approach

Maven is used to:

- Compile the Java source code
- Download project dependencies
- Copy the UTF-8 resource bundles into the application
- Run automated tests
- Generate JaCoCo code coverage reports
- Enforce 100% line and branch coverage for `TemperatureConverter`
- Build an executable shaded JAR file

A Dockerfile is included to create a containerized version of the application. The Docker build uses a multi-stage build, runs `mvn clean verify` (including tests and JaCoCo checks), and packages the application together with its dependencies. The runtime image uses a UTF-8 locale so the localized texts are handled correctly.

A Jenkins pipeline is also included. It performs the following stages:

1. Checkout
2. Build and test (`mvn clean verify`)
3. Publish test results
4. Publish coverage results
5. Archive the JaCoCo report
6. Build the Docker image
7. Verify the Docker image
8. Publish the image to Docker Hub

The pipeline pushes `junkkari/temperature-converter-gui:latest` using the Jenkins credentials ID `dockerhub-credentials`.

A Kubernetes manifest is provided in `temperature-converter.yaml`. It defines a Deployment that runs the Docker image with an X11 `DISPLAY` variable so the JavaFX window can be shown through XMing or another X server.

## 4. Testing & Quality Assurance Steps

### Automated tests

The project uses JUnit 5 for automated testing.

The tests cover the following functionality:

| Test area | Test scenarios | Expected result |
|---|---|---|
| Fahrenheit-to-Celsius conversion | `32°F`, `212°F`, `-40°F`, `0°F`, `98.6°F`, `451°F` | Correct Celsius values are returned |
| Celsius-to-Fahrenheit conversion | `0°C`, `100°C`, `-40°C`, `37°C`, `-273.15°C`, `50°C` | Correct Fahrenheit values are returned |
| Conversion round-trip | Several Celsius values converted to Fahrenheit and back | The original value is returned within the tolerance |
| Kelvin-to-Celsius conversion | `300 K`, `273.15 K`, and `0 K` | Correct Celsius values are returned |
| Extreme temperature detection | Boundary values `-40` and `50`, edge cases `-41` and `51`, and interior values | Correctly identifies extreme temperatures |
| Localization bundles | All five locales | Every bundle contains all required UI keys |
| UTF-8 encoding | Japanese and Arabic bundles | Non-Latin texts are read correctly without Unicode escapes |
| Language selection | Language codes such as `ja_JP`, `fi-FI`, `sv_SE`, and `ar-SA` | Correct `Language` value is resolved |
| Locale-aware formatting | English, Finnish, and Arabic result patterns | Decimal separators and digits follow the active locale |
| Database connection | Initialize the database and request a connection | A valid database connection is returned |
| Temperature unit DAO | Retrieve available temperature units | Three units are returned in the expected order |
| Temperature record DAO | Save one or more conversions and read them back | All values and timestamps are stored correctly |

The test suite contains 34 JUnit 5 tests. The temperature conversion tests use a tolerance of `0.001` to account for floating-point calculation differences.

JaCoCo enforces 100% line and branch coverage for `TemperatureConverter`. The generated report currently shows 100% line and branch coverage for the default package and 96% instruction / 100% branch coverage overall. The JavaFX entry points (`TempConverterApp`, `TempConverterController`, and `Launcher`) are excluded from the coverage report because they require a display; they are verified manually.

### Manual testing

The graphical user interface can be tested manually using the following scenarios:

1. Enter a valid Celsius value and convert it to Fahrenheit.
2. Enter a valid Fahrenheit value and convert it to Celsius.
3. Enter a valid Kelvin value and convert it to Celsius.
4. Convert a temperature between all supported unit combinations.
5. Leave the input empty and click **Convert**.
6. Enter non-numeric input and click **Convert**.
7. Confirm that an error message is displayed for invalid input.
8. Confirm that valid conversions are displayed with two decimal places.
9. Confirm that a conversion creates a record in the H2 database.
10. Change the language with the language selector and confirm that the labels, the window title, and the unit names update immediately.
11. Select Arabic and confirm that the layout switches to right-to-left and the result uses Arabic text and digits.

### Build verification

The following Maven commands are used for quality assurance:

```bash
mvn clean verify
mvn test
mvn jacoco:report
```

`mvn clean verify` runs the tests, generates the JaCoCo report, checks the coverage rules, and builds the shaded JAR. The coverage report is written to `target/site/jacoco/index.html`, and the Surefire report is written to `target/reports/surefire.html`.

The Maven build also creates a shaded JAR containing the application and its dependencies.

## 5. How to Run

### Prerequisites

Install the following software:

- JDK 17 or newer
- Apache Maven 3.9 or newer
- Git

For the Docker option:

- Docker Desktop or Docker Engine

### Clone the repository

```bash
git clone https://github.com/MWxzy/Ohjelmistotuotantoprojekti2.git
cd Ohjelmistotuotantoprojekti1
```

### Run the automated tests

```bash
mvn test
```

### Build the application

```bash
mvn clean verify
```

The generated JAR file is created in the `target` directory.

### Run the application with Maven

On Windows, run:

```bash
mvn javafx:run
```

The Maven configuration uses the Windows JavaFX platform by default.

On Linux, use:

```bash
mvn -Djavafx.platform=linux javafx:run
```

When the application starts, the language can be changed from the language selector in the main window. The default is English.

### Run the packaged JAR

After building the project, run:

```bash
java -jar target/OTP1_inclass1_assignment-1.0-SNAPSHOT.jar
```

The initial language can be selected with a system property or a command-line argument. The language can still be changed at runtime from the language selector:

```bash
java -Dapp.locale=ja_JP -jar target/OTP1_inclass1_assignment-1.0-SNAPSHOT.jar
java -jar target/OTP1_inclass1_assignment-1.0-SNAPSHOT.jar --lang=fi_FI
```

Supported language codes are `en_US`, `fi_FI`, `sv_SE`, `ja_JP`, and `ar_SA` (the short forms `en`, `fi`, `sv`, `ja`, and `ar` are also accepted).

### Build and run with Docker

Build the Docker image:

```bash
docker build -t temperature-converter-gui .
```

The Docker build runs the unit tests, generates the JaCoCo report, and verifies the coverage rules before packaging the application.

Run the container with XMing on Windows. Start XMing first, allow client connections, and then run:

```bash
docker run --rm -e DISPLAY=host.docker.internal:0.0 -e JAVA_TOOL_OPTIONS='-Dprism.order=sw' temperature-converter-gui
```

On Linux, use the X11 socket:

```bash
docker run --rm -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix -e JAVA_TOOL_OPTIONS='-Dprism.order=sw' temperature-converter-gui
```

The Docker image uses Java 17 and installs the native libraries required by JavaFX. The runtime container uses a UTF-8 locale for the localized texts.

### Run with Kubernetes and Minikube

Start Minikube and load the image into the cluster:

```bash
minikube start
minikube image load junkkari/temperature-converter-gui:latest
```

Apply the provided manifest:

```bash
kubectl apply -f temperature-converter.yaml
kubectl get pods
```

The manifest sets `DISPLAY=host.docker.internal:0.0` so the JavaFX window can be shown through XMing. If the image is published to Docker Hub, `minikube image load` can be omitted and Kubernetes pulls `junkkari/temperature-converter-gui:latest` directly.

The deployment was verified with Minikube on Windows. The pod reached the `Running` state and the application window was displayed through Xming. See `docs/kubernetes-xming.png`.

To remove the deployment:

```bash
kubectl delete -f temperature-converter.yaml
```

## 6. Assignment Screenshots

The `docs` directory contains screenshots produced from the final build:

### Arabic localized application running inside Xming

![Arabic localization in Docker with Xming](docs/docker-xming-ar.png)

### Arabic localized application on Windows

![Arabic localization](docs/localization-ar.png)

### Japanese localized application

![Japanese localization](docs/localization-ja.png)

### Minikube cluster and running deployment

![Minikube status and running pod](docs/kubernetes-status.png)

### Application deployed with Minikube and shown through Xming

![Kubernetes deployment running through Xming](docs/kubernetes-xming.png)

### Successful test execution

![Surefire test report](docs/surefire-tests.png)

### JaCoCo coverage report

![JaCoCo coverage](docs/jacoco-coverage.png)

### JaCoCo default package details

![JaCoCo default package](docs/jacoco-default-package.png)

## 7. In-class Assignment 6 Submission

- GitHub repository: <https://github.com/MWxzy/Ohjelmistotuotantoprojekti2>
- Docker Hub image: `junkkari/temperature-converter-gui:latest`
- Docker Hub page: <https://hub.docker.com/r/junkkari/temperature-converter-gui>
- Xming screenshots: `docs/docker-xming-ar.png` and `docs/kubernetes-xming.png`
- Minikube verification screenshot: `docs/kubernetes-status.png`
- Localization screenshots: `docs/localization-ar.png` and `docs/localization-ja.png`
- Test and coverage screenshots: `docs/surefire-tests.png`, `docs/jacoco-coverage.png`, and `docs/jacoco-default-package.png`
- Kubernetes manifest: `temperature-converter.yaml`

The Docker Hub image is published by the Jenkins pipeline using the `dockerhub-credentials` credentials ID.

## Project Structure

```text
.
├── data/
│   ├── tempconverter.mv.db
│   └── tempconverter.trace.db
├── docs/
│   ├── docker-xming-ar.png
│   ├── jacoco-coverage.png
│   ├── jacoco-default-package.png
│   ├── kubernetes-status.png
│   ├── kubernetes-xming.png
│   ├── localization-ar.png
│   ├── localization-ja.png
│   └── surefire-tests.png
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── I18n.java
│   │   │   ├── Language.java
│   │   │   ├── Launcher.java
│   │   │   ├── TempConverterApp.java
│   │   │   ├── TempConverterController.java
│   │   │   ├── TemperatureConverter.java
│   │   │   └── database/
│   │   │       ├── DatabaseManager.java
│   │   │       ├── TempRecordDAO.java
│   │   │       └── TemperatureUnitDAO.java
│   │   └── resources/
│   │       ├── TempConverter.fxml
│   │       ├── MessagesBundle.properties
│   │       ├── MessagesBundle_en_US.properties
│   │       ├── MessagesBundle_fi_FI.properties
│   │       ├── MessagesBundle_sv_SE.properties
│   │       ├── MessagesBundle_ja_JP.properties
│   │       └── MessagesBundle_ar_SA.properties
│   └── test/
│       └── java/
│           ├── I18nTest.java
│           ├── TemperatureConverterTest.java
│           └── database/
│               ├── DatabaseManagerTest.java
│               ├── TemperatureUnitDAOTest.java
│               └── TempRecordDAOTest.java
├── .dockerignore
├── Dockerfile
├── Jenkinsfile
├── pom.xml
├── temperature-converter.yaml
└── README.md
```