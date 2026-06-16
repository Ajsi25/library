package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class App extends Application {
    private Connection connection;
    private Stage mainStage;
    private String currentUser = "";

    private final TextField idField = new TextField();
    private final TextField titleField = new TextField();
    private final TextField authorField = new TextField();
    private final TextField yearField = new TextField();
    private final TextField genreField = new TextField();
    private final TextField priceField = new TextField();
    private final Label statusLabel = new Label();
    private final TableView<Book> table = new TableView<>();
    private final ObservableList<Book> books = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        mainStage = stage;
        if (connectDatabase()) {
            showLoginScene();
        }
    }

    private void showLoginScene() {
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Label loginStatus = new Label();

        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        styleField(usernameField);
        styleField(passwordField);

        Button loginButton = createButton("Login", "#1565C0");
        Button signupButton = createButton("Sign Up", "#2E7D32");

        loginButton.setOnAction(e -> login(usernameField, passwordField, loginStatus));
        signupButton.setOnAction(e -> signUp(usernameField, passwordField, loginStatus));

        Label title = new Label("Libraria");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(12);
        form.addRow(0, label("Username:"), usernameField);
        form.addRow(1, label("Password:"), passwordField);
        GridPane.setHgrow(usernameField, Priority.ALWAYS);
        GridPane.setHgrow(passwordField, Priority.ALWAYS);

        HBox buttons = new HBox(10, loginButton, signupButton);
        buttons.setAlignment(Pos.CENTER);

        VBox card = new VBox(18, title, form, buttons, loginStatus);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(430);
        card.setPadding(new Insets(28));
        card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 8;
                -fx-border-color: #E5E7EB;
                -fx-border-radius: 8;
                """);

        BorderPane root = new BorderPane(card);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #F3F4F6;");

        mainStage.setTitle("Libraria");
        mainStage.setMinWidth(520);
        mainStage.setMinHeight(420);
        mainStage.setScene(new Scene(root, 560, 430));
        mainStage.show();
    }

    private void showLibraryScene() {
        configureFields();
        configureTable();

        Button insertButton = createButton("Shto", "#2E7D32");
        Button selectButton = createButton("Shfaq", "#1565C0");
        Button updateButton = createButton("Ndrysho", "#EF6C00");
        Button deleteButton = createButton("Fshi", "#B71C1C");
        Button clearButton = createButton("Pastro", "#455A64");
        Button logoutButton = createButton("Dil", "#374151");

        insertButton.setOnAction(e -> insertBook());
        selectButton.setOnAction(e -> selectBooks());
        updateButton.setOnAction(e -> updateBook());
        deleteButton.setOnAction(e -> deleteBook());
        clearButton.setOnAction(e -> clearFields());
        logoutButton.setOnAction(e -> {
            currentUser = "";
            clearFields();
            books.clear();
            showLoginScene();
        });

        table.setOnMouseClicked(e -> fillFieldsFromSelection());

        Label title = new Label("Libraria");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        Label userLabel = new Label("User: " + currentUser);
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4B5563;");

        HBox header = new HBox(14, title, userLabel, logoutButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 8, 0));
        HBox.setHgrow(title, Priority.ALWAYS);

        GridPane form = createForm();
        HBox buttons = new HBox(10, insertButton, selectButton, updateButton, deleteButton, clearButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox formPanel = new VBox(14, new Label("Te dhenat e librit"), form, buttons, statusLabel);
        formPanel.setPadding(new Insets(18));
        formPanel.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 8;
                -fx-border-color: #E5E7EB;
                -fx-border-radius: 8;
                """);
        ((Label) formPanel.getChildren().get(0)).setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        VBox center = new VBox(14, formPanel, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(center);
        root.setPadding(new Insets(22));
        root.setStyle("-fx-background-color: #F3F4F6;");

        selectBooks();
        mainStage.setTitle("Libraria");
        mainStage.setMinWidth(900);
        mainStage.setMinHeight(640);
        mainStage.setScene(new Scene(root, 940, 680));
    }

    private boolean connectDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:libraria.db");
            connection.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL
                    )
                    """);
            connection.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS books (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        author TEXT NOT NULL,
                        year INTEGER NOT NULL,
                        genre TEXT NOT NULL,
                        price REAL NOT NULL
                    )
                    """);
            return true;
        } catch (SQLException e) {
            Label error = new Label("Gabim ne lidhje me databazen: " + e.getMessage());
            error.setStyle("-fx-text-fill: #B71C1C; -fx-font-weight: bold;");
            mainStage.setScene(new Scene(new VBox(20, error), 520, 180));
            mainStage.show();
            return false;
        }
    }

    private void login(TextField usernameField, PasswordField passwordField, Label loginStatus) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            setLabel(loginStatus, "Ploteso username dhe password.", "#EF6C00");
            return;
        }

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT id FROM users WHERE username = ? AND password = ?"
            );
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                currentUser = username;
                showLibraryScene();
            } else {
                setLabel(loginStatus, "Username ose password gabim.", "#B71C1C");
            }
        } catch (SQLException e) {
            setLabel(loginStatus, "Gabim gjate login: " + e.getMessage(), "#B71C1C");
        }
    }

    private void signUp(TextField usernameField, PasswordField passwordField, Label loginStatus) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.length() < 3 || password.length() < 3) {
            setLabel(loginStatus, "Username dhe password duhet te kene te pakten 3 karaktere.", "#EF6C00");
            return;
        }

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (username, password) VALUES (?, ?)"
            );
            statement.setString(1, username);
            statement.setString(2, password);
            statement.executeUpdate();
            setLabel(loginStatus, "Account u krijua. Tani shtyp Login.", "#1B5E20");
        } catch (SQLException e) {
            setLabel(loginStatus, "Ky username ekziston ose ka gabim ne DB.", "#B71C1C");
        }
    }

    private void configureFields() {
        idField.setPromptText("ID per ndryshim/fshirje");
        titleField.setPromptText("Titulli i librit");
        authorField.setPromptText("Autori");
        yearField.setPromptText("Viti");
        genreField.setPromptText("Zhanri");
        priceField.setPromptText("Cmimi");

        for (TextField field : new TextField[]{idField, titleField, authorField, yearField, genreField, priceField}) {
            styleField(field);
        }
    }

    private void styleField(TextField field) {
        field.setMinHeight(36);
        field.setStyle("""
                -fx-background-color: #F9FAFB;
                -fx-border-color: #D1D5DB;
                -fx-border-radius: 6;
                -fx-background-radius: 6;
                -fx-padding: 8 10;
                """);
    }

    private void configureTable() {
        table.getColumns().clear();

        table.getColumns().add(column("ID", "id", 70));
        table.getColumns().add(column("Titulli", "title", 260));
        table.getColumns().add(column("Autori", "author", 190));
        table.getColumns().add(column("Viti", "year", 90));
        table.getColumns().add(column("Zhanri", "genre", 150));
        table.getColumns().add(column("Cmimi", "price", 110));
        table.setItems(books);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("Nuk ka ende libra ne databaze."));
        table.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 8;
                -fx-border-color: #E5E7EB;
                -fx-border-radius: 8;
                """);
    }

    private <T> TableColumn<Book, T> column(String title, String property, double width) {
        TableColumn<Book, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    private GridPane createForm() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);

        grid.addRow(0, label("ID:"), idField, label("Titulli:"), titleField);
        grid.addRow(1, label("Autori:"), authorField, label("Viti:"), yearField);
        grid.addRow(2, label("Zhanri:"), genreField, label("Cmimi:"), priceField);

        for (TextField field : new TextField[]{idField, titleField, authorField, yearField, genreField, priceField}) {
            GridPane.setHgrow(field, Priority.ALWAYS);
            field.setMaxWidth(Double.MAX_VALUE);
        }
        return grid;
    }

    private Label label(String text) {
        Label label = new Label(text);
        label.setMinWidth(82);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
        return label;
    }

    private Button createButton(String text, String color) {
        Button button = new Button(text);
        button.setMinWidth(105);
        button.setMinHeight(36);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        return button;
    }

    private void insertBook() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());
            String genre = genreField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());

            if (title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
                setStatus("Titulli, autori dhe zhanri nuk mund te jene bosh.", "#EF6C00");
                return;
            }

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO books (title, author, year, genre, price) VALUES (?, ?, ?, ?, ?)"
            );
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setInt(3, year);
            statement.setString(4, genre);
            statement.setDouble(5, price);
            statement.executeUpdate();

            setStatus("Libri u shtua me sukses.", "#1B5E20");
            clearFields();
            selectBooks();
        } catch (NumberFormatException e) {
            setStatus("Viti dhe cmimi duhet te jene numra.", "#EF6C00");
        } catch (SQLException e) {
            setStatus("Gabim gjate INSERT: " + e.getMessage(), "#B71C1C");
        }
    }

    private void selectBooks() {
        try {
            books.clear();
            ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT id, title, author, year, genre, price FROM books ORDER BY id"
            );

            while (resultSet.next()) {
                books.add(new Book(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("author"),
                        resultSet.getInt("year"),
                        resultSet.getString("genre"),
                        resultSet.getDouble("price")
                ));
            }
            setStatus("Te dhenat u shfaqen. Gjithsej: " + books.size() + " libra.", "#1565C0");
        } catch (SQLException e) {
            setStatus("Gabim gjate SELECT: " + e.getMessage(), "#B71C1C");
        }
    }

    private void updateBook() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            int year = Integer.parseInt(yearField.getText().trim());
            String genre = genreField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());

            if (title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
                setStatus("Ploteso te gjitha fushat per ndryshim.", "#EF6C00");
                return;
            }

            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE books SET title=?, author=?, year=?, genre=?, price=? WHERE id=?"
            );
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setInt(3, year);
            statement.setString(4, genre);
            statement.setDouble(5, price);
            statement.setInt(6, id);

            int rows = statement.executeUpdate();
            setStatus(rows > 0 ? "Libri u ndryshua me sukses." : "Nuk u gjet asnje liber me kete ID.", "#1B5E20");
            clearFields();
            selectBooks();
        } catch (NumberFormatException e) {
            setStatus("ID, viti dhe cmimi duhet te jene numra.", "#EF6C00");
        } catch (SQLException e) {
            setStatus("Gabim gjate UPDATE: " + e.getMessage(), "#B71C1C");
        }
    }

    private void deleteBook() {
        try {
            int id = Integer.parseInt(idField.getText().trim());
            PreparedStatement statement = connection.prepareStatement("DELETE FROM books WHERE id=?");
            statement.setInt(1, id);

            int rows = statement.executeUpdate();
            setStatus(rows > 0 ? "Libri u fshi me sukses." : "Nuk u gjet asnje liber me kete ID.", "#1B5E20");
            clearFields();
            selectBooks();
        } catch (NumberFormatException e) {
            setStatus("ID duhet te jete numer.", "#EF6C00");
        } catch (SQLException e) {
            setStatus("Gabim gjate DELETE: " + e.getMessage(), "#B71C1C");
        }
    }

    private void fillFieldsFromSelection() {
        Book selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        idField.setText(String.valueOf(selected.getId()));
        titleField.setText(selected.getTitle());
        authorField.setText(selected.getAuthor());
        yearField.setText(String.valueOf(selected.getYear()));
        genreField.setText(selected.getGenre());
        priceField.setText(String.valueOf(selected.getPrice()));
    }

    private void setStatus(String message, String color) {
        setLabel(statusLabel, message, color);
    }

    private void setLabel(Label label, String message, String color) {
        label.setText(message);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private void clearFields() {
        table.getSelectionModel().clearSelection();
        idField.clear();
        titleField.clear();
        authorField.clear();
        yearField.clear();
        genreField.clear();
        priceField.clear();
    }

    @Override
    public void stop() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Book {
        private final int id;
        private final String title;
        private final String author;
        private final int year;
        private final String genre;
        private final double price;

        public Book(int id, String title, String author, int year, String genre, double price) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.year = year;
            this.genre = genre;
            this.price = price;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public int getYear() {
            return year;
        }

        public String getGenre() {
            return genre;
        }

        public double getPrice() {
            return price;
        }
    }
}
