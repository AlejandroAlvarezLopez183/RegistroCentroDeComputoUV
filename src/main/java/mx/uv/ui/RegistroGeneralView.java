package mx.uv.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import mx.uv.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RegistroGeneralView {

    public void mostrar(Stage parentStage) {
        Stage stage = new Stage();
        stage.setTitle("Registro de Uso General (No Clases)");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);

        // Campos
        TextField txtMatricula = new TextField();
        TextField txtNombre = new TextField();
        TextField txtCarrera = new TextField();
        TextField txtProposito = new TextField(); // Ej: Tarea, Investigación
        Spinner<Integer> spinHoras = new Spinner<>(1, 5, 1);

        // Añadir al grid
        grid.addRow(0, new Label("Matrícula:"), txtMatricula);
        grid.addRow(1, new Label("Nombre:"), txtNombre);
        grid.addRow(2, new Label("Carrera:"), txtCarrera);
        grid.addRow(3, new Label("Propósito:"), txtProposito);
        grid.addRow(4, new Label("Horas estimadas:"), spinHoras);

        Button btnRegistrar = new Button("Registrar Entrada");
        btnRegistrar.setMaxWidth(Double.MAX_VALUE);
        btnRegistrar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

        btnRegistrar.setOnAction(e -> {
            if (txtMatricula.getText().isEmpty() || txtNombre.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Faltan datos obligatorios");
                alert.show();
                return;
            }
            guardarAsistencia(
                    txtMatricula.getText(),
                    txtNombre.getText(),
                    txtCarrera.getText(),
                    txtProposito.getText(),
                    spinHoras.getValue()
            );
            stage.close();
        });

        grid.add(btnRegistrar, 0, 5, 2, 1); // Span 2 columnas

        stage.setScene(new Scene(grid, 400, 350));
        stage.initOwner(parentStage);
        stage.show();
    }

    private void guardarAsistencia(String mat, String nom, String car, String prop, int horas) {
        String sql = "INSERT INTO AsistenciaCentroDeComputo (Nombre, Apellido, Carrera, Matricula, Proposito, Horas, FechaHora, Asistencia) VALUES (?, ?, ?, ?, ?, ?, ?, 1)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nom);
            pstmt.setString(2, ""); // Apellido opcional en este form simple
            pstmt.setString(3, car);
            pstmt.setString(4, mat);
            pstmt.setString(5, prop);
            pstmt.setInt(6, horas);
            pstmt.setString(7, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            pstmt.executeUpdate();

            new Alert(Alert.AlertType.INFORMATION, "Entrada registrada correctamente").show();

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error de BD: " + e.getMessage()).show();
        }
    }
}