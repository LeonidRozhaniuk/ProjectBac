import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class MainGUI extends Application {
    private static final long serialVersionUID = 1L;
    public static Stage primaryStage;

    private static final String PATH_SIX = "Tickets.txt";

    public void start(Stage primaryStage) {
        // Створюємо елементи інтерфейсу
        Button userBtn = new Button("Користувач");
        userBtn.setStyle("-fx-min-width: 100px;");
        Button adminBtn = new Button("Адмiнiстратор");
        adminBtn.setStyle("-fx-min-width: 100px;");
        Button cashierBtn = new Button("Касир");
        cashierBtn.setStyle("-fx-min-width: 100px;");
        Button exitBtn = new Button("Вихід");
        exitBtn.setStyle("-fx-min-width: 100px;");

        // Встановлюємо обробники подій для кнопок
        userBtn.setOnAction(event -> userCheck(primaryStage));
        adminBtn.setOnAction(event -> adminCheck(primaryStage));
        cashierBtn.setOnAction(event -> cashierCheck(primaryStage));
        exitBtn.setOnAction(event -> System.exit(0));

        // Створюємо VBox і додаємо до нього елементи інтерфейсу
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(userBtn, adminBtn, cashierBtn, exitBtn, createRegistrationLink());

        // Створюємо сцену та встановлюємо її на вікно
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Театр");
        primaryStage.show();
    }

    private Hyperlink createRegistrationLink() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 75, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Ім'я користувача");
        PasswordField password = new PasswordField();
        password.setPromptText("Пароль");

        grid.add(new Label("Ім'я користувача:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Пароль:"), 0, 1);
        grid.add(password, 1, 1);

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Реєстрація користувача");
        dialog.setHeaderText(null);
        dialog.getDialogPane().setContent(grid);

        ButtonType registerBtnType = new ButtonType("Зареєструватись", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerBtnType, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == registerBtnType) {
                String enteredUsername = username.getText().trim();
                String enteredPassword = password.getText().trim();
                if (!enteredUsername.matches("^[a-zA-Z]+$")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Помилка реєстрації");
                    alert.setHeaderText(null);
                    alert.setContentText("Ім'я користувача повинно бути на англійській");
                    alert.showAndWait();
                    return null;
                }
                try {
                    BufferedReader reader = new BufferedReader(new FileReader("users.txt"));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":");
                        if (parts[0].equals(enteredUsername)) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Помилка реєстрації");
                            alert.setHeaderText(null);
                            alert.setContentText("Ім'я користувача вже використовується.");
                            alert.showAndWait();
                            return null;
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Помилка реєстрації");
                    alert.setHeaderText(null);
                    alert.setContentText("Будь ласка, введіть ім'я користувача та пароль.");
                    alert.showAndWait();
                    return null;
                } else {
                    try {
                        FileWriter writer = new FileWriter("users.txt", true);
                        writer.write(enteredUsername + ":" + enteredPassword + "\n");
                        writer.close();
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Успішна реєстрація");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("Користувача успішно зареєстровано!");
                        successAlert.showAndWait();
                        return enteredUsername;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            } else {
                return null;
            }
        });

        Hyperlink registrationLink = new Hyperlink("Ще не зареєструвались?");
        registrationLink.setOnAction(event -> dialog.showAndWait());
        return registrationLink;
    }

    private void userCheck(Stage primaryStage) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Вхід користувача");
        dialog.setHeaderText("Будь ласка, введіть свій логін та пароль.");
        dialog.setContentText("Логін:");
        Optional<String> result1 = dialog.showAndWait();
        if (!result1.isPresent()) {
            return;
        }
        String username = result1.get();
        dialog.setContentText("Пароль:");
        Optional<String> result2 = dialog.showAndWait();
        if (!result2.isPresent()) {
            return;
        }
        String password = result2.get();

        boolean userFound = false;
        try {
            File file = new File("users.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":");
                String savedUsername = parts[0];
                String savedPassword = parts[1];
                if (savedUsername.equals(username) && savedPassword.equals(password)) {
                    userFound = true;
                    break;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (userFound) {
            user(primaryStage, username);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка входу");
            alert.setHeaderText("Невірний логін або пароль.");
            alert.setContentText("Будь ласка, спробуйте ще раз.");
            alert.showAndWait();
        }
    }


    private void adminCheck(Stage primaryStage) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Вхід адміністратора");
        dialog.setHeaderText("Будь ласка, введіть свій логін та пароль.");
        dialog.setContentText("Логін:");
        Optional<String> result1 = dialog.showAndWait();
        if (!result1.isPresent()) {
            return;
        }
        String username = result1.get();
        dialog.setContentText("Пароль:");
        Optional<String> result2 = dialog.showAndWait();
        if (!result2.isPresent()) {
            return;
        }
        String password = result2.get();

        if (username.equals("admin") && password.equals("IFNTUOG")) {
            admin(primaryStage);
        } else {
            // Якщо логін та пароль невірні, показуємо повідомлення про помилку
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка входу");
            alert.setHeaderText("Невірний логін або пароль.");
            alert.setContentText("Будь ласка, спробуйте ще раз.");
            alert.showAndWait();
        }
    }

    private void cashierCheck(Stage primaryStage) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Вхід касира");
        dialog.setHeaderText("Будь ласка, введіть свій логін та пароль.");
        dialog.setContentText("Логін:");
        Optional<String> result1 = dialog.showAndWait();
        if (!result1.isPresent()) {
            return;
        }
        String username = result1.get();
        dialog.setContentText("Пароль:");
        Optional<String> result2 = dialog.showAndWait();
        if (!result2.isPresent()) {
            return;
        }
        String password = result2.get();

        if (username.equals("cashier") && password.equals("IFNTUOG")) {
            cashier(primaryStage);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка входу");
            alert.setHeaderText("Невірний логін або пароль.");
            alert.setContentText("Будь ласка, спробуйте ще раз.");
            alert.showAndWait();
        }
    }
    public void user(Stage primaryStage, String username) {
        Label label = new Label("Ви увійшли як Користувач");
        Button backButton = new Button("Повернутися");
        backButton.setStyle("-fx-min-width: 250px;");
        Button actorsButton = new Button("Переглянути список акторів");
        actorsButton.setStyle("-fx-min-width: 250px;");
        Button musiciansButton = new Button("Переглянути список музикантів");
        musiciansButton.setStyle("-fx-min-width: 250px;");
        Button employeesButton = new Button("Переглянути список працівників театру");
        employeesButton.setStyle("-fx-min-width: 250px;");
        Button spectaclesButton = new Button("Переглянути список вистав");
        spectaclesButton.setStyle("-fx-min-width: 250px;");
        Button searchButton = new Button("Пошук вистав за критеріями");
        searchButton.setStyle("-fx-min-width: 250px;");
        Button authorsButton = new Button("Переглянути список авторів");
        authorsButton.setStyle("-fx-min-width: 250px;");
        Button buyTicketsButton = new Button("Придбати квиток");
        buyTicketsButton.setStyle("-fx-min-width: 250px;");

        backButton.setOnAction(event -> start(primaryStage));
        actorsButton.setOnAction(event -> readActors());
        musiciansButton.setOnAction(event -> readMusicians());
        employeesButton.setOnAction(event -> readEmployees());
        spectaclesButton.setOnAction(event -> readSpectacle());
        searchButton.setOnAction(event -> findSpectacles(primaryStage));
        authorsButton.setOnAction(event -> readAuthors());
        buyTicketsButton.setOnAction(event -> BuyTickets(primaryStage, username));

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(label, actorsButton, musiciansButton, employeesButton,
                spectaclesButton, searchButton, authorsButton, buyTicketsButton, backButton);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Користувач");
        primaryStage.show();
    }


    public void admin(Stage primaryStage) {
        primaryStage.setTitle("Admin Panel");
        Label label = new Label("Виберіть опцію:");

        Button addActorButton = new Button("Додати актора");
        addActorButton.setOnAction(e -> ActorsWrite(primaryStage));
        addActorButton.setStyle("-fx-min-width: 250px;");

        Button addMusicianButton = new Button("Додати музиканта");
        addMusicianButton.setOnAction(e -> MusicianWrite(primaryStage));
        addMusicianButton.setStyle("-fx-min-width: 250px;");

        Button addEmployeeButton = new Button("Додати працівника театру");
        addEmployeeButton.setOnAction(e -> EmployeeWrite(primaryStage));
        addEmployeeButton.setStyle("-fx-min-width: 250px;");

        Button addSpectacleButton = new Button("Додати виставу");
        addSpectacleButton.setOnAction(e -> spectacle(primaryStage));
        addSpectacleButton.setStyle("-fx-min-width: 250px;");

        Button searchActorButton = new Button("Пошук акторів за критеріями");
        searchActorButton.setOnAction(e -> findActors(primaryStage));
        searchActorButton.setStyle("-fx-min-width: 250px;");

        Button searchMusicianButton = new Button("Пошук музикантів за критеріями");
        searchMusicianButton.setOnAction(e -> findMusicians(primaryStage));
        searchMusicianButton.setStyle("-fx-min-width: 250px;");

        Button searchEmployeeButton = new Button("Пошук працівників театру за критеріями");
        searchEmployeeButton.setOnAction(e -> findEmployers(primaryStage));
        searchEmployeeButton.setStyle("-fx-min-width: 250px;");

        Button searchSpectacleButton = new Button("Пошук вистав за критеріями");
        searchSpectacleButton.setOnAction(e -> findSpectacles(primaryStage));
        searchSpectacleButton.setStyle("-fx-min-width: 250px;");

        Button addAuthorButton = new Button("Додати автора");
        addAuthorButton.setOnAction(e -> AuthorsWrite(primaryStage));
        addAuthorButton.setStyle("-fx-min-width: 250px;");

        Button searchAuthorButton = new Button("Пошук автора");
        searchAuthorButton.setOnAction(e -> findAuthors(primaryStage));
        searchAuthorButton.setStyle("-fx-min-width: 250px;");

        Button searchRoleButton = new Button("Пошук актора для ролі");
        searchRoleButton.setOnAction(e -> findRole(primaryStage));
        searchRoleButton.setStyle("-fx-min-width: 250px;");

        Button showRolesButton = new Button("Перегляд переліку ролей в виставах");
        showRolesButton.setOnAction(e -> readRoles());
        showRolesButton.setStyle("-fx-min-width: 250px;");

        Button backButton = new Button("Повернутися");
        backButton.setOnAction(e -> start(primaryStage));
        backButton.setStyle("-fx-min-width: 250px;");

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(label, addActorButton, addMusicianButton, addEmployeeButton, addSpectacleButton,
                searchActorButton, searchMusicianButton, searchEmployeeButton, searchSpectacleButton, addAuthorButton,
                searchAuthorButton, searchRoleButton, showRolesButton, backButton);
        vbox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vbox, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void cashier(Stage primaryStage) {
        primaryStage.setTitle("Касир");
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Label label = new Label("Виберіть опцію:");
        Button addBtn = new Button("Додати виставу з квиткiв на продаж");
        addBtn.setOnAction(e -> TicketsWrite());
        addBtn.setStyle("-fx-min-width: 250px;");

        Button searchBtn = new Button("Пошук проданих квиткiв за критерiями");
        searchBtn.setOnAction(e -> findTickets(primaryStage));
        searchBtn.setStyle("-fx-min-width: 250px;");

        Button sumBtn = new Button("Сума виручених грошей з продажу квиткiв");
        sumBtn.setOnAction(e -> SumOfTickets(primaryStage));
        sumBtn.setStyle("-fx-min-width: 250px;");

        Button backBtn = new Button("Повернутися");
        backBtn.setOnAction(e -> start(primaryStage));
        backBtn.setStyle("-fx-min-width: 250px;");

        vbox.getChildren().addAll(label, addBtn, searchBtn, sumBtn, backBtn);

        Scene scene = new Scene(vbox, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void TicketsWrite() {
        Stage stage = new Stage();
        stage.setTitle("Додавання вистави");

        VBox container = new VBox();
        container.setPadding(new Insets(10, 10, 10, 10));
        container.setSpacing(10);

        Label nameLabel = new Label("Назва вистави:");
        javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();
        container.getChildren().addAll(nameLabel, nameField);

        Label premierOrNotLabel = new Label("Прем'єра:");
        ChoiceBox<String> premierOrNotBox = new ChoiceBox<>(FXCollections.observableArrayList("Так", "Ні"));
        container.getChildren().addAll(premierOrNotLabel, premierOrNotBox);

        Label priceLabel = new Label("Ціна за квиток:");
        TextField priceField = new TextField();
        container.getChildren().addAll(priceLabel, priceField);

        Label dateLabel = new Label("Дата оновлення:");
        javafx.scene.control.TextField dateField = new javafx.scene.control.TextField();
        container.getChildren().addAll(dateLabel, dateField);

        Label ticketsLabel = new Label("Кількість квитків:");
        Spinner<Integer> ticketsSpinner = new Spinner<>(1, Integer.MAX_VALUE, 1);
        container.getChildren().addAll(ticketsLabel, ticketsSpinner);

        Button addButton = new Button("Додати виставу");
        addButton.setOnAction(event -> {
            try (FileWriter writer = new FileWriter("Tickets.txt", true)) {
                String name = nameField.getText();
                String premierOrNot = premierOrNotBox.getValue();
                String date = dateField.getText();
                int tickets = ticketsSpinner.getValue();
                int price = Integer.parseInt(priceField.getText());

                String collect = "\nВистава " + name + " | ";
                if (premierOrNot.equals("Так")) {
                    collect += "Прем'єра " + premierOrNot + " | ";
                }
                collect += "Дата оновлення " + date + " | " + "\nКількість доступних квитків/місць " + tickets
                        + "\nЦіна за квиток " + price;

                writer.write(collect);
                writer.write("\n---------------------------------------------------------------------------------------------------------\n");

                nameField.clear();
                premierOrNotBox.setValue("Так");
                dateField.clear();
                ticketsSpinner.getValueFactory().setValue(1);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        });
        container.getChildren().add(addButton);

        Scene scene = new Scene(container);
        stage.setScene(scene);
        stage.show();
    }

    private static final String TICKET_FILE_PATH = "Tickets.txt";
    private static final String TICKETS_BOUGHT_FILENAME = "TicketsBuyed.txt";

    private void findTickets(Stage primaryStage) {
        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        Label resultLabel;
        resultLabel = new Label("");
        Button searchByNameButton = new Button("Пошук за назвою вистави");
        Button showAllButton = new Button("Список всіх вистав");
        Button showTicketsSoldButton = new Button("Список вистав та кількості проданих квитків");
        Button backButton = new Button("Повернутись");
        backButton.setOnAction(event -> cashier(primaryStage));
        root.getChildren().addAll(searchByNameButton, showAllButton, showTicketsSoldButton, scrollPane, backButton);
        scrollPane.setContent(resultArea);

        searchByNameButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Пошук за назвою вистави");
            dialog.setHeaderText(null);
            dialog.setContentText("Введіть назву вистави:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                boolean found = false; // змінна, що відповідає за наявність знайденої вистави
                try (BufferedReader br = new BufferedReader(new FileReader(TICKET_FILE_PATH))) {
                    StringBuilder resultStringBuilder = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.contains(name)) {
                            found = true;
                            resultStringBuilder.append(line).append("\n");
                            for (int i = 0; i < 2; i++) {
                                line = br.readLine();
                                resultStringBuilder.append(line).append("\n");
                            }
                            resultStringBuilder.append("-------------------------------------------\n");
                        }
                    }
                    if (found) {
                        resultArea.setText(resultStringBuilder.toString());
                    } else {
                        resultArea.setText("Вистава з назвою '" + name + "' не знайдена");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });

        showAllButton.setOnAction(event -> {
            try (BufferedReader br = new BufferedReader(new FileReader(TICKET_FILE_PATH))) {
                StringBuilder resultStringBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    resultStringBuilder.append(line).append("\n");
                    for (int i = 0; i < 2; i++) {
                        line = br.readLine();
                        resultStringBuilder.append(line).append("\n");
                    }
                }
                resultArea.setText(resultStringBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        showTicketsSoldButton.setOnAction(event -> {
            int count = 0;
            double total = 0;
            Map<String, Double> revenueMap = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new FileReader(TICKETS_BOUGHT_FILENAME))) {
                String line;
                String currentShow = "";
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("Вистава")) {
                        currentShow = line.replaceAll("^.*?(?= за )", "");
                        line = br.readLine();
                        int tickets = Integer.parseInt(line.replaceAll("\\D", ""));
                        count += tickets;
                        line = br.readLine();
                        double revenue = Double.parseDouble(line.replaceAll("\\D+", ""));
                        total += revenue;
                        revenueMap.merge(currentShow, revenue, Double::sum);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringBuilder result = new StringBuilder();
            result.append(String.format("Кількість проданих квитків: %d\nСума виручки: %.2f\n\n", count, total));
            result.append("Вистави:\n");
            revenueMap.forEach((show, revenue) -> result.append(show).append(": ").append(String.format("%.2f", revenue)).append("\n"));

            try (BufferedReader br = new BufferedReader(new FileReader(TICKETS_BOUGHT_FILENAME))) {
                result.append("\nВміст файлу TicketsBuyed.txt:\n");
                String line;
                while ((line = br.readLine()) != null) {
                    result.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            resultArea.setText(result.toString());
        });
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Пошук квитків");
        primaryStage.show();
    }

    private static final String FILENAME = "TicketsBuyed.txt";

    private void SumOfTickets(Stage primaryStage) {
        Label resultLabel;
        Label searchLabel = new Label("Введіть назву вистави:");
        TextField searchField = new TextField();
        Button searchButton = new Button("Пошук");
        Button backButton = new Button("Повернутися");
        resultLabel = new Label("");
        ScrollPane scrollPane = new ScrollPane(resultLabel);

        VBox root = new VBox(10, searchLabel, searchField, searchButton, scrollPane, backButton);
        root.setPadding(new Insets(10));
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.setTitle("Сума виручки");
        primaryStage.show();

        searchButton.setOnAction(event -> {
            String searchValue = searchField.getText();
            int count = 0;
            double total = 0;
            Map<String, Double> revenueMap = new HashMap<>();
            try (BufferedReader br = new BufferedReader(new FileReader(FILENAME))) {
                String line;
                String currentShow = "";
                for (; (line = br.readLine()) != null; ) {
                    if (line.contains(searchValue)) {
                        currentShow = line;
                        line = br.readLine();
                        int tickets = Integer.parseInt(line.replaceAll("\\D", ""));
                        count += tickets;
                        line = br.readLine();
                        double revenue = Double.parseDouble(line.replaceAll("\\D+", ""));
                        total += revenue;
                        revenueMap.merge(currentShow, revenue, Double::sum);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            StringBuilder result = new StringBuilder();
            result.append(String.format("Кількість квитків: %d\nСума виручки: %.2f\n\n", count, total));
            result.append("Суми виручок за виставами:\n");
            revenueMap.forEach((show, revenue) -> result.append(show).append(": ").append(String.format("%.2f", revenue)).append("\n"));

            resultLabel.setText(result.toString());
        });

        backButton.setOnAction(event -> {
            cashier(primaryStage);
        });

    }

    private void ActorsWrite(Stage primaryStage) {
        Button submitButton;
        TextField numberField;
        TextField nameField;
        TextField surnameField;
        TextField rankField;
        TextField yearField;
        TextField monthField;
        TextField examsField;
        TextField workYearsField;
        TextField spectaclesField;
        TextField nameSpecField;
        TextField producerField;
        TextField genresField;
        TextField showOnYearField;
        TextField showOnField;
        TextField gastrolField;
        TextField spectaclField;
        TextField yeargasField;
        TextField monthgasField;
        TextField malefemaleField;
        TextField birthField;
        TextField yearsField;
        TextField childrenField;
        TextField paymentField;
        Label numberLabel = new Label("Порядковий номер:");
        numberField = new TextField();
        Label nameLabel = new Label("Ім'я актора:");
        nameField = new TextField();

        Label surnameLabel = new Label("Прізвище актора:");
        surnameField = new TextField();

        Label rankLabel = new Label("Звання актора:");
        rankField = new TextField();

        Label yearLabel = new Label("Рік отримання звання:");
        yearField = new TextField();

        Label monthLabel = new Label("Місяць отримання звання:");
        monthField = new TextField();

        Label examsLabel = new Label("Конкурс на якому отримано звання:");
        examsField = new TextField();

        Label workYearsLabel = new Label("Стаж роботи актора в театрі:");
        workYearsField = new TextField();

        Label spectaclesLabel = new Label("Виконані актором ролі:");
        spectaclesField = new TextField();

        Label nameSpecLabel = new Label("Назва спектаклю:");
        nameSpecField = new TextField();

        Label producerLabel = new Label("Режисер-постановник вистави:");
        producerField = new TextField();

        Label genresLabel = new Label("Жанр вистави:");
        genresField = new TextField();

        Label showOnYearLabel = new Label("Рік коли спектакль поставлявся:");
        showOnYearField = new TextField();

        Label showOnLabel = new Label("Місяць коли спектакль поставлявся:");
        showOnField = new TextField();

        Label gastrolLabel = new Label("Чи приїздив коли-небудь на гастролі до театру:");
        gastrolField = new TextField();

        Label spectaclLabel = new Label("З яким спектаклем був приїзд:");
        spectaclField = new TextField();

        Label yeargasLabel = new Label("Рік коли був приїзд на гастролі:");
        yeargasField = new TextField();

        Label monthgasLabel = new Label("Місяць коли був приїзд на гастролі:");
        monthgasField = new TextField();
        Label malefemaleLabel = new Label("Стать актора:");
        malefemaleField = new TextField();

        Label birthLabel = new Label("Дата народження актора:");
        birthField = new TextField();

        Label yearsLabel = new Label("Вік актора:");
        yearsField = new TextField();

        Label childrenLabel = new Label("Кількість дітей у актора:");
        childrenField = new TextField();

        Label paymentLabel = new Label("Середня зарплата актора:");
        paymentField = new TextField();

        submitButton = new Button("Додати актора");
        Button backButton = new Button("Повернутися");
        ScrollPane scrollPane = new ScrollPane();
        VBox root = new VBox();
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true);

        root.getChildren().addAll(
                numberLabel, numberField,
                nameLabel, nameField,
                surnameLabel, surnameField,
                rankLabel, rankField,
                yearLabel, yearField,
                monthLabel, monthField,
                examsLabel, examsField,
                workYearsLabel, workYearsField,
                spectaclesLabel, spectaclesField,
                nameSpecLabel, nameSpecField,
                producerLabel, producerField,
                genresLabel, genresField,
                showOnYearLabel, showOnYearField,
                showOnLabel, showOnField,
                gastrolLabel, gastrolField,
                spectaclLabel, spectaclField,
                yeargasLabel, yeargasField,
                monthgasLabel, monthgasField,
                malefemaleLabel, malefemaleField,
                birthLabel, birthField,
                yearsLabel, yearsField,
                childrenLabel, childrenField,
                paymentLabel, paymentField,
                submitButton, backButton
        );

        Scene scene = new Scene(scrollPane, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Додавання актора");
        primaryStage.show();
        submitButton.setOnAction(event -> {
            try {
                FileWriter writer = new FileWriter("Actors.txt", true);
                String number = numberField.getText();
                String name = nameField.getText();
                String surname = surnameField.getText();
                String rank = rankField.getText();
                String year = yearField.getText();
                String month = monthField.getText();
                String exams = examsField.getText();
                String workYears = workYearsField.getText();
                String spectacles = spectaclesField.getText();
                String nameSpec = nameSpecField.getText();
                String producer = producerField.getText();
                String genres = genresField.getText();
                String showOnYear = showOnYearField.getText();
                String showOn = showOnField.getText();
                String gastrol = gastrolField.getText();
                String spectacl = spectaclField.getText();
                String yeargas = yeargasField.getText();
                String monthgas = monthgasField.getText();
                String malefemale = malefemaleField.getText();
                String birth = birthField.getText();
                String years = yearsField.getText();
                String children = childrenField.getText();
                String payment = paymentField.getText();


                String actorInfo = "№ " + number + ". " + "Iм'я " + name + " | " + "Прiзвище " + surname + " | " + "Звання " + rank + " | " + "Рiк отримання " + year + " | " + "Мiсяць отримання " + month + " | " + "Конкурс на якому отримано " + exams + " | " + "Стаж роботи " + workYears + " | " + "Ролi актора" + spectacles + " | " + "Назва спектаклю " + nameSpec + " | " + "Режисер-поставник " + producer + " | " + "Жанр " + genres + " | " + "Рiк показу спектаклю " + showOnYear + " | " + "Мiсяць показу спектаклю " + showOn + " | " + "Чи приїздив коли-небудь на гастролi до театру " + gastrol + " | " + "З яким спектаклем був приїзд " + spectacl + " | " + "Рiк приїзду " + yeargas + " | " + "Мiсяць приїзду " + monthgas + " | " + "Стать " + malefemale + " | " + "Рiк народження " + birth + " | " + "Вiк " + years + " | " + "Кiлькiсть дiтей " + children + " | " + "Розмiр заробiтньої плати " + payment;
                writer.write(actorInfo);
                writer.write("\n---------------------------------------------------------------------------------------------------------\n");

                writer.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Актора успішно додано!");
                alert.showAndWait();

                nameField.clear();
                surnameField.clear();
                rankField.clear();
                yearField.clear();
                monthField.clear();
                examsField.clear();
                workYearsField.clear();
                spectaclesField.clear();
                nameSpecField.clear();
                producerField.clear();
                genresField.clear();
                showOnYearField.clear();
                showOnField.clear();
                gastrolField.clear();
                spectaclField.clear();
                yeargasField.clear();
                monthgasField.clear();
                malefemaleField.clear();
                birthField.clear();
                yearsField.clear();
                childrenField.clear();
                paymentField.clear();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        backButton.setOnAction(event -> {
            admin(primaryStage);
        });
    }

    public void MusicianWrite(Stage primaryStage) {
        Label numberLabel = new Label("Порядковий номер музиканта:");
        javafx.scene.control.TextField numberField = new javafx.scene.control.TextField();
        Label nameLabel = new Label("Iм'я музиканта:");
        javafx.scene.control.TextField nameField = new javafx.scene.control.TextField();
        Label surnameLabel = new Label("Прiзвище музиканта:");
        javafx.scene.control.TextField surnameField = new javafx.scene.control.TextField();
        Label workYearsLabel = new Label("Стаж роботи музиканта в театрi:");
        javafx.scene.control.TextField workYearsField = new javafx.scene.control.TextField();
        Label malefemaleLabel = new Label("Стать музиканта:");
        javafx.scene.control.TextField malefemaleField = new javafx.scene.control.TextField();
        Label birthLabel = new Label("Рiк народження музиканта:");
        javafx.scene.control.TextField birthField = new javafx.scene.control.TextField();
        Label yearsLabel = new Label("Вiк музиканта:");
        javafx.scene.control.TextField yearsField = new javafx.scene.control.TextField();
        Label childrenLabel = new Label("Кiлькiсть дiтей у музиканта:");
        javafx.scene.control.TextField childrenField = new javafx.scene.control.TextField();
        Label paymentLabel = new Label("Розмiр заробiтної плати музиканта:");
        javafx.scene.control.TextField paymentField = new javafx.scene.control.TextField();
        Button submitBtn = new Button("Додати музиканта");
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(
                numberLabel, numberField,
                nameLabel, nameField,
                surnameLabel, surnameField,
                workYearsLabel, workYearsField,
                malefemaleLabel, malefemaleField,
                birthLabel, birthField,
                yearsLabel, yearsField,
                childrenLabel, childrenField,
                paymentLabel, paymentField,
                submitBtn
        );

        Scene scene = new Scene(root, 800, 600);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Додати музиканта");
        stage.show();

        submitBtn.setOnAction(event -> {
            try {
                String number = numberField.getText();
                String name = nameField.getText();
                String surname = surnameField.getText();
                String workYears = workYearsField.getText();
                String malefemale = malefemaleField.getText();
                String birth = birthField.getText();
                String years = yearsField.getText();
                String children = childrenField.getText();
                String payment = paymentField.getText();
                String collect = number + ". " + "Iм'я " + name + " | " + "Прiзвище " + surname + " | " + "Стаж роботи " + workYears + " | " + "Стать " + malefemale + " | " + "Рiк народження " + birth + " | " + "Вiк " + years + " | " + "Кiлькiсть дiтей " + children + " | " + "Розмiр заробiтньої плати " + payment;
                BufferedWriter writer = new BufferedWriter(new FileWriter("Musicians.txt", true));
                writer.write(collect);
                writer.newLine();
                writer.write("---------------------------------------------------------------------------------------------------------");
                writer.newLine();
                writer.close();

                JOptionPane.showMessageDialog(null, "Музиканта додано успішно.");

                numberField.setText("");
                nameField.setText("");
                surnameField.setText("");
                workYearsField.setText("");
                malefemaleField.setText("");
                birthField.setText("");
                yearsField.setText("");
                childrenField.setText("");
                paymentField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Будь ласка, введіть коректні дані.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Сталася помилка при записі в файл.");
            }
        });
    }


    private void EmployeeWrite(Stage primaryStage) {
        TextField numberField;
        TextField nameField;
        TextField surnameField;
        TextField jobnameField;
        CheckBox gastrolCheckBox;
        TextField spectaclField;
        TextField yearField;
        TextField monthField;
        TextField workYearsField;
        TextField malefemaleField;
        TextField birthField;
        TextField yearsField;
        TextField childrenField;
        TextField paymentField;
        Label numberLabel = new Label("Порядковий номер робiтника:");
        numberField = new TextField();
        Label nameLabel = new Label("Iм'я робiтника:");
        nameField = new TextField();
        Label surnameLabel = new Label("Прiзвище робiтника:");
        surnameField = new TextField();
        Label jobnameLabel = new Label("Посада робiтника:");
        jobnameField = new TextField();
        Label gastrolLabel = new Label("Чи приїздив коли-небудь на гастролi до театру:");
        TextField gastrolField = new TextField();
        Label spectaclLabel = new Label("З яким спектаклем був приїзд:");
        spectaclField = new TextField();
        Label yearLabel = new Label("Рiк коли був приїзд на гастролi:");
        yearField = new TextField();
        Label monthLabel = new Label("Мiсяць коли був приїзд на гастролi:");
        monthField = new TextField();
        Label workYearsLabel = new Label("Стаж роботи робiтника в театрi:");
        workYearsField = new TextField();
        Label malefemaleLabel = new Label("Стать робiтника:");
        malefemaleField = new TextField();
        Label birthLabel = new Label("Рiк народження робiтника:");
        birthField = new TextField();
        Label yearsLabel = new Label("Вiк робiтника:");
        yearsField = new TextField();
        Label childrenLabel = new Label("Кiлькiсть дiтей у робiтника:");
        childrenField = new TextField();
        Label paymentLabel = new Label("Розмiр заробiтної плати робiтника:");
        paymentField = new TextField();
        Button saveButton = new Button("Зберегти");
        Button backBtn = new Button("Повернутися");
        backBtn.setOnAction(e -> admin(primaryStage));
        saveButton.setOnAction(event -> {
            try (FileWriter writer = new FileWriter("Employees.txt", true)) {
                int number = Integer.parseInt(numberField.getText());
                String name = nameField.getText();
                String surname = surnameField.getText();
                String jobname = jobnameField.getText();
                String gastrol = gastrolField.getText();
                String spectacl = spectaclField.getText();
                String year = yearField.getText();
                String month = monthField.getText();
                int workYears = Integer.parseInt(workYearsField.getText());
                String malefemale = malefemaleField.getText();
                int birth = Integer.parseInt(birthField.getText());
                int years = Integer.parseInt(yearsField.getText());
                int children = Integer.parseInt(childrenField.getText());
                int payment = Integer.parseInt(paymentField.getText());

                String collect = number + ". " + "Iм'я " + name + " | " + "Прiзвище " + surname + " | " + "Посада " + jobname + " | " + "Чи приїздив коли-небудь на гастролi до театру " + gastrol + " | " + "З яким спектаклем був приїзд " + spectacl + " | " + "Рiк приїзду " + year + " | " + "Мiсяць приїзду " + month + " | " + "Стаж роботи " + workYears + " | " + "Стать " + malefemale + " | " + "Рiк народження " + birth + " | " + "Вiк " + years + " | " + "Кiлькiсть дiтей " + children + " | " + "Розмiр заробiтньої плати " + payment;

                writer.write(collect);
                writer.write("\n---------------------------------------------------------------------------------------------------------\n");
                numberField.clear();
                nameField.clear();
                surnameField.clear();
                jobnameField.clear();
                gastrolField.clear();
                spectaclField.clear();
                yearField.clear();
                monthField.clear();
                workYearsField.clear();
                malefemaleField.clear();
                birthField.clear();
                yearsField.clear();
                childrenField.clear();
                paymentField.clear();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Інформація");
                alert.setHeaderText("Збережено");
                alert.showAndWait();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        });


        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.getChildren().addAll(numberLabel, numberField, nameLabel, nameField, surnameLabel, surnameField,
                jobnameLabel, jobnameField, gastrolLabel, gastrolField, spectaclLabel, spectaclField, yearLabel,
                yearField, monthLabel, monthField, workYearsLabel, workYearsField, malefemaleLabel, malefemaleField,
                birthLabel, birthField, yearsLabel, yearsField, childrenLabel, childrenField, paymentLabel,
                paymentField, saveButton, backBtn);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(vbox);
        scrollPane.setFitToWidth(true);

        VBox container = new VBox(scrollPane);
        container.setPrefHeight(Region.USE_COMPUTED_SIZE);

        Scene scene = new Scene(container, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static final String PATH = "Actors.txt";

    private void findActors(Stage primaryStage) {

        Label label = new Label("Введіть число від 1 до 13:");
        Label labell = new Label("0 - повернутися" +
                "\n1 - Пошук за стажем роботи" +
                "\n2 - Пошук за статевою ознакою" +
                "\n3 - Пошук за роком народження" +
                "\n4 - Пошук за вiком" + "\n5 - Пошук за кiлькiстю дiтей\n6 - Пошук за розмiром заробiтньої плати" +
                "\n7 - Пошук за званням" +
                "\n8 - Пошук за званням, отриманих в перiод часу(Рiк, мiсяць)" +
                "\n9 - Пошук акторiв з званням отриманих на необхiдному конкурсi" +
                "\n10 - Пошук акторiв та постановникiв якi приїздили на гастролi до театру коли-небудь\n11 - Пошук пошук ролей зiграних актором за весь час" +
                "\n12 - Пошук пошук ролей зiграних актором за окремий перiод час" +
                "\n13 - Пошук пошук ролей зiграних актором за режисером-постановщиком");
        TextField textField = new TextField();
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);

        Button searchButton = new Button("Пошук");
        Button returne = new Button("Повернутися");
        searchButton.setOnAction(event -> {
            resultArea.clear();
            final String[] sub = {""};
            AtomicReference<String> subone = new AtomicReference<>("");
            int mode = Integer.parseInt(textField.getText());

            switch (mode) {
                case 0:
                    admin(primaryStage);
                case 1:
                    sub[0] = "Стаж роботи " + getTextInput("Введіть робочий стаж:\n");
                    break;
                case 2:
                    sub[0] = "Стать " + getTextInput("Введіть стать:");
                    break;
                case 3:
                    sub[0] = "Рiк народження " + getTextInput("Введіть рік народження:");
                    break;
                case 4:
                    sub[0] = "Вiк " + getTextInput("Введіть вік:");
                    break;
                case 5:
                    sub[0] = "Кiлькiсть дiтей " + getTextInput("Введіть кількість дітей:");
                    break;
                case 6:
                    sub[0] = "Розмiр заробiтньої плати " + getTextInput("Введіть розмір заробітньої плати:");
                    break;
                case 7:
                    sub[0] = "Звання " + getTextInput("Введіть звання:");
                    break;
                case 8:
                    resultArea.clear();
                    Label label1 = new Label("Введіть звання:");
                    TextField textField1 = new TextField();
                    Label label2 = new Label("Введіть рік коли звання було отримано:");
                    TextField textField2 = new TextField();
                    Label label3 = new Label("Введіть місяць коли звання було отримано:");
                    TextField textField3 = new TextField();
                    Button searchButton1 = new Button("Пошук");
                    Button ret = new Button("Повернутися");
                    searchButton1.setOnAction(event1 -> {
                        sub[0] = "Звання " + textField1.getText().trim();
                        String year = "Рiк отримання " + textField2.getText().trim();
                        String month = "Мiсяць отримання " + textField3.getText().trim();
                        resultArea.clear();
                        try (Scanner reader = new Scanner(new File(PATH))) {
                            while (reader.hasNextLine()) {
                                String lineFile = reader.nextLine();
                                if (lineFile.contains(sub[0]) && lineFile.contains(year) && lineFile.contains(month)) {
                                    resultArea.appendText(lineFile + "\n");
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    });

                    ret.setOnAction((event1 -> {
                        findActors(primaryStage);
                    }));
                    VBox vbox1 = new VBox(10);
                    vbox1.getChildren().addAll(label1, textField1, label2, textField2, label3, textField3, searchButton1, resultArea, ret);
                    vbox1.setAlignment(Pos.CENTER);
                    Scene scene1 = new Scene(vbox1, 500, 500);
                    primaryStage.setScene(scene1);
                    primaryStage.show();
                    break;
                case 9:
                    Label label4 = new Label("Введіть конкурс на якому було отримано звання:");
                    TextField textField4 = new TextField();
                    Button searchButton2 = new Button("Пошук");
                    Button ret1 = new Button("Повернутися");
                    searchButton2.setOnAction(event1 -> {
                        sub[0] = "Конкурс на якому отримано " + textField4.getText().trim();
                        resultArea.clear();
                        try (Scanner reader = new Scanner(new File(PATH))) {
                            while (reader.hasNextLine()) {
                                String lineFile = reader.nextLine();
                                if (lineFile.contains(sub[0])) {
                                    resultArea.appendText(lineFile + "\n");
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    });
                    ret1.setOnAction((event1 -> {
                        findActors(primaryStage);
                    }));
                    VBox vbox2 = new VBox(10);
                    vbox2.getChildren().addAll(label4, textField4, searchButton2, resultArea, ret1);
                    vbox2.setAlignment(Pos.CENTER);
                    Scene scene2 = new Scene(vbox2, 500, 500);
                    primaryStage.setScene(scene2);
                    primaryStage.show();
                    break;
                case 10:
                    Label label10_1 = new Label("З яким спектаклем був приїзд:");
                    TextField playField = new TextField();
                    Label label10_2 = new Label("Введіть рік приїзду:");
                    TextField yearField = new TextField();
                    Label label10_3 = new Label("Введіть місяць приїзду:");
                    TextField monthField = new TextField();
                    Button searchButton10 = new Button("Пошук");
                    Button ret2 = new Button("Повернутися");
                    searchButton10.setOnAction(event3 -> {
                        String play = "З яким спектаклем був приїзд " + playField.getText().trim();
                        String year = "Рiк приїзду " + yearField.getText().trim();
                        String month = "Мiсяць приїзду " + monthField.getText().trim();
                        resultArea.clear();
                        try (Scanner reader = new Scanner(new File(PATH))) {
                            while (reader.hasNextLine()) {
                                String lineFile = reader.nextLine();
                                if (lineFile.contains(play) && lineFile.contains(year) && lineFile.contains(month)) {
                                    resultArea.appendText(lineFile + "\n");
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        try (Scanner reader = new Scanner(new File(PATH_THREE))) {
                            while (reader.hasNextLine()) {
                                String lineFile = reader.nextLine();
                                if (lineFile.contains(play) && lineFile.contains(year) && lineFile.contains(month)) {
                                    resultArea.appendText(lineFile + "\n");
                                }
                            }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    });

                    ret2.setOnAction((event1 -> {
                        findActors(primaryStage);
                    }));

                    VBox vbox10 = new VBox(10);
                    vbox10.getChildren().addAll(label10_1, playField, label10_2, yearField, label10_3, monthField, searchButton10, resultArea, ret2);
                    vbox10.setAlignment(Pos.CENTER);
                    Scene scene10 = new Scene(vbox10, 500, 500);
                    primaryStage.setScene(scene10);
                    primaryStage.show();
                    break;
                case 11:
                    Label label11_1 = new Label("Введіть ім'я актора:");
                    TextField firstNameField = new TextField();
                    Label label11_2 = new Label("Введіть прізвище актора:");
                    TextField lastNameField = new TextField();
                    Button searchButton11 = new Button("Пошук");
                    Button ret3 = new Button("Повернутися");
                    searchButton11.setOnAction(event4 -> {
                        String firstName = "Iм'я " + firstNameField.getText().trim();
                        String lastName = "Прiзвище " + lastNameField.getText().trim();
                        resultArea.clear();
                        try (Scanner reader = new Scanner(new File(PATH))) {
                            while (reader.hasNextLine()) {
                                String lineFile = reader.nextLine();
                                if (lineFile.contains(firstName) && lineFile.contains(lastName)) {
                                    resultArea.appendText(lineFile + "\n");
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    });

                    ret3.setOnAction((event1 -> {
                        findActors(primaryStage);
                    }));

                    VBox vbox11 = new VBox(10);
                    vbox11.getChildren().addAll(label11_1, firstNameField, label11_2, lastNameField, searchButton11, resultArea, ret3);
                    vbox11.setAlignment(Pos.CENTER);
                    Scene scene11 = new Scene(vbox11, 500, 500);
                    primaryStage.setScene(scene11);
                    primaryStage.show();
                    break;
                case 12:
                    Label label5 = new Label("Введіть рік показу спектаклю:");
                    TextField textField5 = new TextField();
                    Label label6 = new Label("Введіть місяць показу спектаклю:");
                    TextField textField6 = new TextField();
                    Button searchButton3 = new Button("Пошук");
                    Button ret4 = new Button("Повернутися");
                    searchButton3.setOnAction(event1 -> {
                        sub[0] = "Рiк показу спектаклю " + textField5.getText().trim();
                        subone.set("Мiсяць показу спектаклю " + textField6.getText().trim());
                        resultArea.clear();
                        try (Scanner reader = new Scanner(new File(PATH))) {
                            while (reader.hasNextLine()) {
                                String lineFile = reader.nextLine();
                                if (lineFile.contains(sub[0]) && lineFile.contains(subone.get())) {
                                    resultArea.appendText(lineFile + "\n");
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    });

                    ret4.setOnAction((event1 -> {
                        findActors(primaryStage);
                    }));


                    VBox vbox3 = new VBox(10);
                    vbox3.getChildren().addAll(label5, textField5, label6, textField6, searchButton3, resultArea, ret4);
                    vbox3.setAlignment(Pos.CENTER);
                    Scene scene3 = new Scene(vbox3, 500, 500);
                    primaryStage.setScene(scene3);
                    primaryStage.show();
                    break;

                case 13:
                    Label label7 = new Label("Введіть режисера-постановника:");
                    TextField textField7 = new TextField();
                    Button searchButton4 = new Button("Пошук");
                    Button ret5 = new Button("Повернутися");
                    searchButton4.setOnAction(event1 -> {
                        sub[0] = "Режисер-поставник " + textField7.getText().trim();
                        resultArea.clear();
                        try (Scanner reader = new Scanner(new File(PATH))) {
                            while (reader.hasNextLine()) {
                                String lineFile = reader.nextLine();
                                if (lineFile.contains(sub[0])) {
                                    resultArea.appendText(lineFile + "\n");
                                }
                            }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    });

                    ret5.setOnAction((event1 -> {
                        findActors(primaryStage);
                    }));

                    VBox vbox4 = new VBox(10);
                    vbox4.getChildren().addAll(label7, textField7, searchButton4, resultArea, ret5);
                    vbox4.setAlignment(Pos.CENTER);
                    Scene scene4 = new Scene(vbox4, 500, 500);
                    primaryStage.setScene(scene4);
                    primaryStage.show();
                    break;
                default:
                    resultArea.appendText("Помилка! Введіть число від 1 до 13");
                    return;
            }


            try (Scanner reader = new Scanner(new File(PATH))) {
                while (reader.hasNextLine()) {
                    String lineFile = reader.nextLine();
                    if (lineFile.contains(sub[0])) {
                        resultArea.appendText(lineFile + "\n");
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        });

        returne.setOnAction((event1 -> {
            admin(primaryStage);
        }));

        VBox root = new VBox();
        root.getChildren().addAll(label, textField, searchButton, resultArea, labell, returne);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("Actors Finder");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private String getTextInput(String prompt) {
        TextField textField = new TextField();
        Label label = new Label(prompt);

        GridPane grid = new GridPane();
        grid.add(label, 0, 0);
        grid.add(textField, 1, 0);

        Stage inputStage = new Stage();
        inputStage.setScene(new Scene(grid, 300, 50));
        inputStage.showAndWait();

        return textField.getText();
    }

    private static final String PATH_THREE = "Employees.txt";

    private void findEmployers(Stage primaryStage) {
        Label label = new Label("Виберіть критерій пошуку:");
        ComboBox<String> searchCriteriaComboBox = new ComboBox<>();
        searchCriteriaComboBox.getItems().addAll(
                "Стаж роботи",
                "Стать",
                "Рік народження",
                "Вік",
                "Кількість дітей",
                "Розмір заробітньої плати",
                "Посада"
        );
        TextField searchTextField = new TextField();
        Button searchButton = new Button("Пошук");
        TextArea resultsTextArea = new TextArea();
        Button backButton = new Button("Повернутися");

        searchButton.setOnAction(event -> {
            resultsTextArea.clear();
            String searchCriteria = searchCriteriaComboBox.getValue();
            String searchValue = searchTextField.getText().trim();

            if (searchCriteria.equals("Стаж роботи")) {
                sub = "Стаж роботи " + searchValue.trim();
            } else if (searchCriteria.equals("Стать")) {
                sub = "Стать " + searchValue.trim();
            } else if (searchCriteria.equals("Рік народження")) {
                sub = "Рiк народження " + searchValue.trim();
            } else if (searchCriteria.equals("Вік")) {
                sub = "Вiк " + searchValue.trim();
            } else if (searchCriteria.equals("Кількість дітей")) {
                sub = "Кiлькiсть дiтей " + searchValue.trim();
            } else if (searchCriteria.equals("Розмір заробітньої плати")) {
                sub = "Розмiр заробiтньої плати " + searchValue.trim();
            } else if (searchCriteria.equals("Посада")) {
                sub = "Посада " + searchValue.trim();
            } else {
                System.out.println("Такого пункту не існує!");
            }

            try (Scanner reader = new Scanner(new File(PATH_THREE))) {
                while (reader.hasNextLine()) {
                    String lineFile = reader.nextLine();
                    if (lineFile.contains(sub)) {
                        resultsTextArea.appendText(lineFile + "\n");
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
        backButton.setOnAction(event -> {
            admin(primaryStage);
        });

        Scene scene = new Scene(new VBox(label, searchCriteriaComboBox, searchTextField, searchButton, resultsTextArea, backButton), 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private static final String PATH_TWO = "Musicians.txt";

    public void findMusicians(Stage primaryStage) {
        // Створення елементів графічного інтерфейсу
        Label label = new Label("Виберіть критерій пошуку:");
        ComboBox<String> searchCriteriaComboBox = new ComboBox<>();
        searchCriteriaComboBox.getItems().addAll(
                "Стаж роботи",
                "Стать",
                "Рік народження",
                "Вік",
                "Кількість дітей",
                "Розмір заробітньої плати"
        );
        TextField searchTextField = new TextField();
        Button searchButton = new Button("Пошук");
        TextArea resultsTextArea = new TextArea();
        Button backButton = new Button("Повернутися");

        searchButton.setOnAction(event -> {
            resultsTextArea.clear();
            String sub = "";
            String searchCriteria = searchCriteriaComboBox.getValue();
            String searchValue = searchTextField.getText().trim();

            if (searchCriteria.equals("Стаж роботи")) {
                sub = "Стаж роботи " + searchValue;
            } else if (searchCriteria.equals("Стать")) {
                sub = "Стать " + searchValue.trim();
            } else if (searchCriteria.equals("Рік народження")) {
                sub = "Рiк народження " + searchValue.trim();
            } else if (searchCriteria.equals("Вік")) {
                sub = "Вiк " + searchValue.trim();
            } else if (searchCriteria.equals("Кількість дітей")) {
                sub = "Кiлькiсть дiтей " + searchValue.trim();
            } else if (searchCriteria.equals("Розмір заробітньої плати")) {
                sub = "Розмiр заробiтньої плати " + searchValue.trim();
            }
            try (Scanner reader = new Scanner(new File(PATH_TWO))) {
                StringBuilder results = new StringBuilder();
                while (reader.hasNextLine()) {
                    String lineFile = reader.nextLine();
                    if (lineFile.contains(sub)) {
                        results.append(lineFile).append("\n");
                    }
                }
                if (results.length() == 0) {
                    resultsTextArea.setText("Не знайдено жодного музиканта за цим критерієм.");
                } else {
                    resultsTextArea.setText("Результати пошуку:\n" + results.toString());
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        backButton.setOnAction(event -> {
            admin(primaryStage);
        });
        VBox root = new VBox();
        root.getChildren().addAll(label, searchCriteriaComboBox, searchTextField, searchButton, resultsTextArea, backButton);


        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private static final String PATH_FIVE = "Authors.txt";
    private final TextField inputField = new TextField();
    private final RadioButton playRadioButton = new RadioButton("Вистава");
    private final RadioButton centuryRadioButton = new RadioButton("Століття");
    private final RadioButton countryRadioButton = new RadioButton("Країна");
    private final RadioButton genreRadioButton = new RadioButton("Жанр");
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final Label statusLabel = new Label();
    private final Button searchButton = new Button("Шукати");
    private TextArea resultArea = new TextArea();
    private void findAuthors(Stage primaryStage) {
        primaryStage.setTitle("Пошук вистав");
        resultArea.setEditable(false);
        playRadioButton.setToggleGroup(toggleGroup);
        centuryRadioButton.setToggleGroup(toggleGroup);
        countryRadioButton.setToggleGroup(toggleGroup);
        genreRadioButton.setToggleGroup(toggleGroup);

        inputField.setPromptText("Введіть критерій пошуку");

        searchButton.setOnAction(event -> search());

        statusLabel.setAlignment(Pos.CENTER);
        Button backButton = new Button("Повернутись");
        backButton.setOnAction(event -> admin(primaryStage));
        VBox radioButtonsBox = new VBox(10, playRadioButton, centuryRadioButton, countryRadioButton, genreRadioButton);
        radioButtonsBox.setPadding(new Insets(10));
        radioButtonsBox.setAlignment(Pos.TOP_LEFT);

        BorderPane borderPane = new BorderPane();
        borderPane.setLeft(radioButtonsBox);
        borderPane.setCenter(inputField);
        VBox statusBox = new VBox(10, resultArea, searchButton,backButton, statusLabel);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(10));
        borderPane.setBottom(statusBox);

        Scene scene = new Scene(borderPane, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void search() {
        resultArea.clear();
        String sub = inputField.getText().trim();
        if (sub.isEmpty()) {
            statusLabel.setText("Введіть критерій пошуку!");
            return;
        }

        RadioButton selectedRadioButton = (RadioButton) toggleGroup.getSelectedToggle();
        if (selectedRadioButton == null) {
            statusLabel.setText("Виберіть критерій пошуку!");
            return;
        }

        try (Scanner reader = new Scanner(new File(PATH_FIVE))) {
            while (reader.hasNextLine()) {
                String lineFile = reader.nextLine();
                if (selectedRadioButton == playRadioButton && lineFile.contains("Вистави автора  " + sub)) {
                    resultArea.appendText(lineFile + "\n");
                } else if (selectedRadioButton == centuryRadioButton && lineFile.contains("Столiття в якому жив автор " + sub)) {
                    resultArea.appendText(lineFile + "\n");
                } else if (selectedRadioButton== countryRadioButton && lineFile.contains("Країна автора " + sub)) {
                    resultArea.appendText(lineFile + "\n");
                } else if (selectedRadioButton == genreRadioButton && lineFile.contains("Жанр " + sub)) {
                    resultArea.appendText(lineFile + "\n");
                }
            }
            if (resultArea.getText().isEmpty()) {
                statusLabel.setText("Нічого не знайдено!");
            } else {
                statusLabel.setText("");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void findRole(Stage primaryStage) {
        final String[] role = new String[1];
        final String[] spec = new String[1];
        final String[] rank = new String[1];
        final String[] sub = new String[1];
        final String[] workYears = new String[1];
        final String[] years = new String[1];
        final String[] name = new String[1];
        final String[] idget = new String[1];
        primaryStage.setTitle("Пошук актора для ролі");

        BorderPane borderPane = new BorderPane();
        borderPane.setPadding(new Insets(10));

        VBox vBox = new VBox();
        vBox.setSpacing(10);

        Label roleLabel = new Label("Роль:");
        TextField roleField = new TextField();
        HBox roleHBox = new HBox(roleLabel, roleField);
        roleHBox.setSpacing(10);

        Label specLabel = new Label("Назва вистави:");
        TextField specField = new TextField();
        HBox specHBox = new HBox(specLabel, specField);
        specHBox.setSpacing(10);

        Label rankLabel = new Label("Звання:");
        TextField rankField = new TextField();
        HBox rankHBox = new HBox(rankLabel, rankField);
        rankHBox.setSpacing(10);

        Label subLabel = new Label("Стать:");
        TextField subField = new TextField();
        HBox subHBox = new HBox(subLabel, subField);
        subHBox.setSpacing(10);

        Label workYearsLabel = new Label("Стаж:");
        TextField workYearsField = new TextField();
        HBox workYearsHBox = new HBox(workYearsLabel, workYearsField);
        workYearsHBox.setSpacing(10);

        Label yearsLabel = new Label("Age:");
        TextField yearsField = new TextField();
        HBox yearsHBox = new HBox(yearsLabel, yearsField);
        yearsHBox.setSpacing(10);

        Button searchButton = new Button("Пошук");
        Button saveButton = new Button("Зберегти");
        Button returnButton = new Button("Повернутись");
        HBox buttonsHBox = new HBox(searchButton, saveButton, returnButton);
        buttonsHBox.setSpacing(10);

        TextArea resultsArea = new TextArea();
        resultsArea.setEditable(false);

        vBox.getChildren().addAll(roleHBox, specHBox, rankHBox, subHBox, workYearsHBox, yearsHBox, buttonsHBox, resultsArea);
        borderPane.setCenter(vBox);

        searchButton.setOnAction(event -> {
            role[0] = roleField.getText();
            spec[0] = specField.getText();
            rank[0] = rankField.getText();
            sub[0] = subField.getText();
            workYears[0] = workYearsField.getText();
            years[0] = yearsField.getText();

            resultsArea.clear();
            resultsArea.appendText("Актор який пiдходить для ролi " + role[0] + " у виставі " + spec[0] + ":\n");

            try (Scanner reader = new Scanner(new File(PATH))) {
                while (reader.hasNextLine()) {
                    String lineFile = reader.nextLine();
                    if (lineFile.contains(sub[0]) && lineFile.contains(workYears[0]) && lineFile.contains(years[0]) && lineFile.contains(rank[0])) {
                        resultsArea.appendText(lineFile + "\n");
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        });

        saveButton.setOnAction(event-> {
            role[0] = roleField.getText();
            spec[0] = specField.getText();
            rank[0] = rankField.getText();
            sub[0] = subField.getText();
            workYears[0] = workYearsField.getText();
            years[0] = yearsField.getText();
            idget[0] = "№ ";
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Введіть порядковий номер");
            dialog.setHeaderText(null);
            dialog.setContentText("Введіть порядковий номер актора, яких підходить для ролі:");
            dialog.showAndWait();
            Optional<String> result = dialog.showAndWait();
            idget[0] = "№ " + result.get();

            try (FileWriter writer = new FileWriter("Roles.txt", true)) {
                try (Scanner reader = new Scanner(new File(PATH))) {
                    while (reader.hasNextLine()) {
                        String lineFile = reader.nextLine();
                        if (lineFile.contains(idget[0])) {
                            System.out.println(lineFile);
                            writer.write("\nАктор який пiдходить для ролi " + role[0] + " у виставi " + spec[0] + ": ");
                            writer.write(lineFile);
                            writer.write("\n---------------------------------------------------------------------------------------------------------\n");
                        }
                    }
                } }  catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        });

        returnButton.setOnAction(event -> {
            admin(primaryStage);
        });

        Scene scene = new Scene(borderPane, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void AuthorsWrite(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        Label lblNumber = new Label("Порядковий номер автора:");
        TextField txtNumber = new TextField();

        Label lblName = new Label("Iм'я автора:");
        TextField txtName = new TextField();

        Label lblSurname = new Label("Прiзвище автора:");
        TextField txtSurname = new TextField();

        Label lblCentury = new Label("Столiття в якому жив автор:");
        TextField txtCentury = new TextField();

        Label lblCountry = new Label("Країна автора:");
        TextField txtCountry = new TextField();

        Label lblSpectacles = new Label("Поставлені автором вистави:");
        TextField txtSpectacles = new TextField();

        Label lblGenres = new Label("Жанр вистави:");
        TextField txtGenres = new TextField();

        Label lblShowOn = new Label("Чи поставлялась вистава у цьому театрi:");
        ComboBox<String> cmbShowOn = new ComboBox<>();
        cmbShowOn.getItems().addAll("Так", "Ні");
        cmbShowOn.setValue("No");

        Label lblMonth = new Label("Мiсяць показу:");
        TextField txtMonth = new TextField();

        Label lblYear = new Label("Рiк показу:");
        TextField txtYear = new TextField();

        Button btnAddSpectacle = new Button("Додати виставу");
        Button backButton = new Button("Повернутися");
        btnAddSpectacle.setOnAction(e -> {
            try (FileWriter writer = new FileWriter("Authors.txt", true)) {
                int number = Integer.parseInt(txtNumber.getText());
                String name = txtName.getText();
                String surname = txtSurname.getText();
                int century = Integer.parseInt(txtCentury.getText());
                String country = txtCountry.getText();
                String collect = number + ". " + "Iм'я " + name + " | " + "Прiзвище " + surname + " | " + "Столiття в якому жив автор " + century + " | " + "Країна автора " + country + " | ";
                writer.write(collect);

                String spectacles = txtSpectacles.getText();
                writer.write(" Вистави автора ");
                writer.write(" " + spectacles);

                String genres = txtGenres.getText();
                writer.write(" Жанр ");
                writer.write(genres);

                String showOn = cmbShowOn.getValue();
                writer.write(" Показ вистави в театрi ");
                writer.write(showOn);

                if (showOn.equalsIgnoreCase("Так")) {
                    String month = txtMonth.getText();
                    writer.write(" Мiсяць показу ");
                    writer.write(month);

                    int year = Integer.parseInt(txtYear.getText());
                    writer.write(" Рiк показу ");
                    writer.write(String.valueOf(year));
                }

                writer.write("\n---------------------------------------------------------------------------------------------------------\n");
                txtNumber.clear();
                txtName.clear();
                txtSurname.clear();
                txtCentury.clear();
                txtCountry.clear();
                txtSpectacles.clear();
                txtGenres.clear();
                txtMonth.clear();
                txtYear.clear();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(10));
            vbox.getChildren().addAll(
                    lblNumber, txtNumber,
                    lblName, txtName,
                    lblSurname, txtSurname,
                    lblCentury, txtCentury,
                    lblCountry, txtCountry,
                    lblSpectacles, txtSpectacles,
                    lblGenres, txtGenres,
                    lblShowOn, cmbShowOn,
                    lblMonth, txtMonth,
                    lblYear, txtYear,
                    btnAddSpectacle, backButton
            );
            primaryStage.setScene(new Scene(vbox, 800, 600));
            primaryStage.show();
        });

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(
                lblNumber, txtNumber,
                lblName, txtName,
                lblSurname, txtSurname,
                lblCentury, txtCentury,
                lblCountry, txtCountry,
                lblSpectacles, txtSpectacles,
                lblGenres, txtGenres,
                lblShowOn, cmbShowOn,
                lblMonth, txtMonth,
                lblYear, txtYear,
                btnAddSpectacle, backButton
        );
        backButton.setOnAction(event -> {
            admin(primaryStage);
        });
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(vbox);
        scrollPane.setFitToWidth(true);

        VBox container = new VBox(scrollPane);
        container.setPrefHeight(Region.USE_COMPUTED_SIZE);

        Scene scene = new Scene(container, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private int n = 1;
    private int year;
    private String monthOne;

    public void spectacle(Stage primaryStage) {
        primaryStage.setTitle("Додавання вистави");

        Label numberLabel = new Label("Порядковий номер:");
        TextField numberField = new TextField();

        Label nameLabel = new Label("Назва вистави:");
        TextField nameField = new TextField();

        Label authorNameLabel = new Label("Автор:");
        TextField authorNameField = new TextField();

        Label authorCountryLabel = new Label("Країна автора:");
        TextField authorCountryField = new TextField();

        Label centuryLabel = new Label("Століття написання:");
        TextField centuryField = new TextField();

        Label genreLabel = new Label("Жанр вистави:");
        TextField genreField = new TextField();

        Label dayLabel = new Label("День проведення:");
        TextField dayField = new TextField();

        Label monthLabel = new Label("Сезон репертуару:");
        TextField monthField = new TextField();

        Label playedLabel = new Label("Статус проведення:");
        TextField playedField = new TextField();

        Label premierDateLabel = new Label("Дата прем'єри:");
        TextField premierDateField = new TextField();

        Label ticketsCountLabel = new Label("Кількість доступних квитків:");
        TextField ticketsCountField = new TextField();

        Label playedOnceLabel = new Label("Проходила в поточному театрі коли-небудь:");
        ComboBox<String> playedOnceBox = new ComboBox<>();
        playedOnceBox.getItems().addAll("Так", "Ні");
        Label year2Label = new Label("Введіть рiк коли вистава проходила вперше:");
        TextField year2Field = new TextField();
        Label month2Label = new Label("Введіть мiсяць коли вистава проходила вперше:");
        TextField month2Field = new TextField();
        playedOnceBox.setPromptText("Виберіть опцію");
        Button backButton = new Button("Повернутися");
        backButton.setOnAction(e -> admin(primaryStage));
        backButton.setStyle("-fx-min-width: 150px;");

        Label prbel = new Label(" ");
        Label prbel2 = new Label(" ");
        Button addButton = new Button("Додати виставу");
        addButton.setStyle("-fx-min-width: 150px;");
        addButton.setOnAction(e -> {
            try (FileWriter writer = new FileWriter("Spectacles.txt", true)) {
                int number = Integer.parseInt(numberField.getText());
                String name = nameField.getText();
                String authorName = authorNameField.getText();
                String authorCountry = authorCountryField.getText();
                int century = Integer.parseInt(centuryField.getText());
                String genre = genreField.getText();
                String day = dayField.getText();
                String month = monthField.getText();
                String played = playedField.getText();
                String premierDate = premierDateField.getText();
                int ticketsCount = Integer.parseInt(ticketsCountField.getText());
                String month2 = month2Field.getText();
                String year2= year2Field.getText();
                String playedOnce = playedOnceBox.getValue();
                if (playedOnce.equals("Так")) {
                    String collect = number + ". " + "Назва вистави " + name + " | " + "Автор " + authorName + " | " + "Країна автора " + authorCountry + " | " + "Столiття коли було написано " + century + " | " + "Жанр вистави " + genre + " | " + "День проведення вистави " + day + " | " + "Сезон репертуару " + month + " | " + "Статус проведення " + played + " | " + "Дата прем'єри " + premierDate + " | " + "Кiлькiсть доступних квиткiв " + ticketsCount + " | " + "Проходила в поточному театрі коли-небудь " + playedOnce +  " | " + "Рiк " + year2 + " | " + "Мiсяць " + month2;
                    writer.write(collect);
                    writer.write(System.lineSeparator());
                } else {
                    String collect = number + ". " + "Назва вистави " + name + " | " + "Автор " + authorName + " | " + "Країна автора " + authorCountry + " | " + "Столiття коли було написано " + century + " | " + "Жанр вистави " + genre + " | " + "День проведення вистави " + day + " | " + "Сезон репертуару " + month + " | " + "Статус проведення " + played + " | " + "Дата прем'єри " + premierDate + " | " + "Кiлькiсть доступних квиткiв " + ticketsCount + " | " + "Проходила в поточному театрі коли-небудь " + playedOnce;
                    writer.write(collect);
                    writer.write(System.lineSeparator());
                }
                numberField.clear();
                nameField.clear();
                authorNameField.clear();
                authorCountryField.clear();
                centuryField.clear();
                genreField.clear();
                dayField.clear();
                monthField.clear();
                playedField.clear();
                premierDateField.clear();
                ticketsCountField.clear();
                month2Field.clear();
                year2Field.clear();
                playedOnceBox.setValue("Ні");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        Scene scene = new Scene(new VBox(numberLabel, numberField, nameLabel, nameField, authorNameLabel, authorNameField, authorCountryLabel, authorCountryField, centuryLabel, centuryField, genreLabel, genreField, dayLabel, dayField, monthLabel, monthField, playedLabel, playedField, premierDateLabel, premierDateField, ticketsCountLabel, ticketsCountField, playedOnceLabel, playedOnceBox,year2Label,year2Field,month2Label,month2Field, prbel2, addButton, prbel, backButton), 1280, 720);
        primaryStage.setScene(scene);

        primaryStage.show();
    }


    private static void readRoles() {
        try (BufferedReader br = new BufferedReader(new FileReader("Roles.txt"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea();
            textArea.setText(sb.toString());
            VBox container = new VBox(textArea);
            Scene scene = new Scene(container, 800, 600);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void readActors() {
        try (BufferedReader br = new BufferedReader(new FileReader("Actors.txt"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea();
            textArea.setText(sb.toString());
            VBox container = new VBox(textArea);
            Scene scene = new Scene(container, 800, 600);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void readMusicians() {
        try (BufferedReader br = new BufferedReader(new FileReader("Musicians.txt"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea();
            textArea.setText(sb.toString());
            VBox container = new VBox(textArea);
            Scene scene = new Scene(container, 800, 600);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void readEmployees() {
        try (BufferedReader br = new BufferedReader(new FileReader("Employees.txt"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea();
            textArea.setText(sb.toString());
            VBox container = new VBox(textArea);
            Scene scene = new Scene(container, 800, 600);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void readSpectacle() {
        try (BufferedReader br = new BufferedReader(new FileReader("Spectacles.txt"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea();
            textArea.setText(sb.toString());
            VBox container = new VBox(textArea);
            Scene scene = new Scene(container, 800, 600);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static final String PATH_FOUR = "Spectacles.txt";

    private void findSpectacles(Stage primaryStage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Пошук вистав");
        dialog.setHeaderText(null);
        dialog.setContentText("Введіть номер запиту:\n" +
                "0 - повернутися\n" +
                "1 - пошук за сезонами репертуару\n" +
                "2 - пошук вже зiграних вистав за необхiдний сезон репертуару\n" +
                "3 - пошук за жанром\n" +
                "4 - пошук зiграних вистав\n" +
                "5 - пошук зiграних вистав необхiдного жанру, якi були поставленi в театрi коли-небудь\n" +
                "6 - пошук вистав окремого автора\n" +
                "7 - пошук вистав по країнi автора\n" +
                "8 - пошук вистав за столiттям\n" +
                "9 - пошук вистав якi були поставленi вперше в необхідний рiк та мiсяць");

        while (true) {
            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) {
                dialog.close();
                start(primaryStage);
                return;
            }
            String input = result.get().trim();

            if (input.equals("0")) {
                admin(primaryStage);
            } else if (input.equals("1")) {
                dialog.getEditor().clear();
                dialog.getEditor().setText("");
                dialog.setResult("");
                dialog.setContentText("Введiть сезон: ");
                result = dialog.showAndWait();
                if (result.isPresent()) {
                    String sub = "Сезон репертуару " + result.get().trim();
                    search(sub, null, primaryStage);
                    dialog.close();
                    return;
                }
            } else if (input.equals("2")) {
                dialog.getEditor().clear();
                dialog.getEditor().setText("");
                dialog.setResult("");
                dialog.setContentText("Сезон репертуару ");
                result = dialog.showAndWait();
                if (result.isPresent()) {
                    String subplay = "Сезон репертуару " + result.get().trim();
                    dialog.setContentText("Статус проведення ");
                    result = dialog.showAndWait();
                    if (result.isPresent()) {
                        String sub = "Статус проведення " + result.get().trim();
                        search(sub, subplay, primaryStage);
                        dialog.close();
                        return;
                    }
                }
            } else if (input.equals("3")) {
                dialog.getEditor().clear();
                dialog.getEditor().setText("");
                dialog.setResult("");
                dialog.setContentText("Введiть жанр вистави: ");
                result = dialog.showAndWait();
                if (result.isPresent()) {
                    String sub = "Жанр вистави " + result.get().trim();
                    search(sub, null, primaryStage);
                    dialog.close();
                    return;
                }
            } else if (input.equals("4")) {
                dialog.getEditor().clear();
                dialog.getEditor().setText("");
                dialog.setResult("");
                if (result.isPresent()) {
                    String sub = "Статус проведення Зіграно";
                    search(sub, null, primaryStage);
                    dialog.close();
                    return;
                }
            } else if (input.equals("5")) {
                dialog.getEditor().clear();
                dialog.getEditor().setText("");
                dialog.setResult("");
                dialog.setContentText("Введiть жанр вистави: ");
                result = dialog.showAndWait();
                if (result.isPresent()) {
                    String subplay = "Жанр вистави " + result.get().trim();
                    String subgenre = "Проходила в поточному театрі коли-небудь Так";
                    search(subplay, subgenre, primaryStage);
                    dialog.close();
                    return;
                }
            } else if (input.equals("6")) {
                dialog.getEditor().clear();
                dialog.getEditor().setText("");
                dialog.setResult("");
                dialog.setContentText("Введiть автора: ");
                result = dialog.showAndWait();
                if (result.isPresent()) {
                    String sub = "Автор " + result.get().trim();
                    search(sub, null, primaryStage);
                    dialog.close();
                    return;
                }
            } else if (input.equals("7")) {
                dialog.getEditor().clear();
                dialog.getEditor().setText("");
                dialog.setResult("");
                dialog.setContentText("Введiть країну автора: ");
                result = dialog.showAndWait();
                if (result.isPresent()) {
                    String sub = "Країна автора " + result.get().trim();
                    search(sub, null, primaryStage);
                    dialog.close();
                    return;
                }
            } else if (input.equals("8")) {
                dialog.getEditor().clear();
                dialog.getEditor().setText("");
                dialog.setResult("");
                dialog.setContentText("Введiть столiття: ");
                result = dialog.showAndWait();
                if (result.isPresent()) {
                    String sub = "Столiття коли було написано " + result.get().trim();
                    search(sub, null, primaryStage);
                    dialog.close();
                    return;
                }
            }else if (input.equals("9")) {
                dialog.getEditor().clear();
                dialog.getEditor().setText("");
                dialog.setResult("");
                dialog.setContentText("Введiть рiк: ");
                result = dialog.showAndWait();
                if (result.isPresent()) {
                    String subyear = "Рiк " + result.get().trim();
                    dialog.setContentText("Введiть мiсяць: ");
                    result = dialog.showAndWait();
                    if (result.isPresent()) {
                        String submonth = "Мiсяць " + result.get().trim();
                        search(subyear, submonth, primaryStage);
                        dialog.close();
                        return;
                    }
                }
            }
        }
    }
    private TextArea textArea;
    private void search(String sub1, String sub2, Stage primaryStage) {
        File file = new File(PATH_FOUR);
        textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(800, 600);
        Button button = new Button("Повернутись");
        button.setOnAction(event -> {
            primaryStage.close();
            findSpectacles(primaryStage);
        });
        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (sub2 != null) {
                    if (line.contains(sub1) && line.contains(sub2)) {
                        textArea.appendText(line);
                        textArea.appendText("\n");
                    }
                } else {
                    if (line.contains(sub1)) {
                        textArea.appendText(line);
                        textArea.appendText("\n");
                    }
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Файл не знайдено: " + PATH_FOUR);
        }
        VBox vBox = new VBox();
        vBox.getChildren().addAll(textArea, button);

        Scene scene = new Scene(vBox);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private static void readAuthors() {
        try (BufferedReader br = new BufferedReader(new FileReader("Authors.txt"))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea();
            textArea.setText(sb.toString());
            VBox container = new VBox(textArea);
            Scene scene = new Scene(container, 800, 600);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private int inputPrice = 175;
    private int i = 0;
    private String sub;
    private int subTick;
    private TextArea outputArea;

    public void BuyTickets(Stage primaryStage, String username) {
        Label titleLabel = new Label("Купівля квитків");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

        Label searchLabel = new Label("Пошук по назві вистави:");
        Label userLabel = new Label("Користувач: " + username);
        TextField searchField = new TextField();
        searchField.setPromptText("Назва вистави");

        Label quantityLabel = new Label("Кількість:");
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 100, 1);
        quantitySpinner.setEditable(true);

        Button searchButton = new Button("Пошук");
        searchButton.setOnAction(event -> {
            try {
                searchTickets(searchField.getText(), quantitySpinner.getValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Button backButton = new Button("Повернутися");
        backButton.setOnAction(event -> user(primaryStage,username));

        outputArea = new TextArea();
        outputArea.setEditable(false);
        TextArea ticketsArea = new TextArea();
        ticketsArea.setEditable(false);
        List<String> ticketsLines = null;
        try {
            ticketsLines = Files.readAllLines(Paths.get("Tickets.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        StringJoiner joiner = new StringJoiner("\n");
        for (String line : ticketsLines) {
            joiner.add(line);
        }
        ticketsArea.setText(joiner.toString());

        GridPane gridPane = new GridPane();
        gridPane.add(ticketsArea, 0, 6, 2, 1);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10));
        gridPane.add(titleLabel, 0, 0, 2, 1);
        gridPane.add(searchLabel, 0, 1);
        gridPane.add(searchField, 1, 1);
        gridPane.add(userLabel, 0, 3);
        gridPane.add(quantityLabel, 0, 2);
        gridPane.add(quantitySpinner, 1, 2);
        gridPane.add(searchButton, 1, 3);
        gridPane.add(backButton, 0, 4);
        gridPane.add(outputArea, 0, 5, 2, 1);

        Scene scene = new Scene(gridPane, 500, 500);

        primaryStage.setTitle("Купівля квитків");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void searchTickets(String input, int quantity) throws IOException {
        outputArea.clear();
        String sub = "Вистава " + input.trim();
        int subTick = quantity;
        boolean found = false;

        List<String> lines = Files.readAllLines(Paths.get("Tickets.txt"));
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains(sub)) {
                String quantityLine = lines.get(i + 1);
                int q = Integer.parseInt(quantityLine.replaceAll("\\D+", ""));
                if (q >= subTick) {
                    found = true;

                    String priceLine = null;
                    for (int j = i + 1; j < lines.size(); j++) {
                        if (lines.get(j).contains("Ціна за квиток")) {
                            priceLine = lines.get(j);
                            break;
                        }
                    }
                    int inputPrice = Integer.parseInt(priceLine.replaceAll("\\D+", ""));

                    int remainingTickets = q - subTick;
                    int price = subTick * inputPrice;
                    Date date = new Date();
                    try (FileWriter writer = new FileWriter("TicketsBuyed.txt", true)) {
                        writer.write("\n" + sub + " за " + date + " було придбано\n " + subTick + " квиткiв" + "\nвиручених грошей " + price);
                    }
                    try (BufferedWriter bw = new BufferedWriter(new FileWriter("Tickets.txt"))) {
                        for (int j = 0; j < lines.size(); j++) {
                            if (j == i + 1) {
                                bw.write("Кількість доступних квитків/місць " + remainingTickets);
                                bw.newLine();
                            } else {
                                bw.write(lines.get(j));
                                bw.newLine();
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
        }

        if (found) {
            outputArea.appendText("Квитки на виставу " + input + " було успішно придбано\n");
            outputArea.appendText("-------------------------------------------------------\n");
            lines = Files.readAllLines(Paths.get("Tickets.txt"));
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                sb.append(line).append("\n");
            }
            outputArea.appendText(sb.toString());
        } else {
            outputArea.appendText("Квитки на виставу " + input + " відсутні або залишилось менше " + subTick + " квитків\n");
            outputArea.appendText("-------------------------------------------------------\n");
            lines = Files.readAllLines(Paths.get("Tickets.txt"));
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                sb.append(line).append("\n");
            }
            outputArea.appendText(sb.toString());
        }
    }

}

