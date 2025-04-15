package org.example.pzris_lab1;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.*;

public class DatabaseQueryApp extends Application {

    private ComboBox<String> connectionTypeCombo;
    private TextArea queryInput;
    private TableView<ObservableList<String>> tableView;
    private Label executionTimeLabel;

    @Override
    public void start(Stage primaryStage) {
        connectionTypeCombo = new ComboBox<>();
        connectionTypeCombo.getItems().addAll("JDBC", "DataSource");
        connectionTypeCombo.setValue("JDBC");

        queryInput = new TextArea("SELECT * FROM employees");
        queryInput.setPrefRowCount(4);

        Button executeButton = new Button("Виконати запит");
        executeButton.setOnAction(e -> executeQuery());

        executionTimeLabel = new Label("Час виконання: -");

        tableView = new TableView<>();

        HBox topPane = new HBox(10, new Label("Підключення:"), connectionTypeCombo);
        topPane.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topPane);
        root.setCenter(tableView);
        root.setBottom(new VBox(10, queryInput, executeButton, executionTimeLabel));
        BorderPane.setMargin(queryInput, new Insets(10));
        BorderPane.setMargin(executeButton, new Insets(0, 10, 0, 10));
        BorderPane.setMargin(executionTimeLabel, new Insets(0, 10, 10, 10));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Database Query App");
        primaryStage.show();
    }

    private void executeQuery() {
        String connectionType = connectionTypeCombo.getValue();
        String query = queryInput.getText();

        try (Connection conn = getConnection(connectionType)) {
            long start = System.nanoTime();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            long end = System.nanoTime();
            long durationMs = (end - start) / 1_000_000;
            executionTimeLabel.setText("Час виконання: " + durationMs + " мс");

            populateTableView(rs);

        } catch (Exception ex) {
            showAlert("Помилка: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Connection getConnection(String type) throws SQLException {
        if ("JDBC".equalsIgnoreCase(type)) {
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/PZRIS", "postgres", "postgres");
        } else if ("DataSource".equalsIgnoreCase(type)) {
            PGSimpleDataSource ds = new PGSimpleDataSource();
            ds.setServerNames(new String[]{"localhost"});
            ds.setPortNumbers(new int[]{5432});
            ds.setDatabaseName("PZRIS");
            ds.setUser("postgres");
            ds.setPassword("postgres");
            return ds.getConnection();
        } else {
            throw new SQLException("Невідомий тип підключення: " + type);
        }
    }

    private void populateTableView(ResultSet rs) throws SQLException {
        tableView.getItems().clear();
        tableView.getColumns().clear();
        ResultSetMetaData metaData = rs.getMetaData();

        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(metaData.getColumnName(i));
            column.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(colIndex - 1)));
            tableView.getColumns().add(column);
        }

        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.add(rs.getString(i));
            }
            tableView.getItems().add(row);
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
