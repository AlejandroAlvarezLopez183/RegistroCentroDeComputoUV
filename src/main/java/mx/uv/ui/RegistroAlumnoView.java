package mx.uv.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mx.uv.db.AlumnoDAO;
import mx.uv.model.Alumno;
import mx.uv.util.GeneradorQR; // Importamos nuestra utilidad nueva
import javafx.scene.image.ImageView;

public class RegistroAlumnoView {

    private final AlumnoDAO alumnoDAO = new AlumnoDAO();

    public void mostrar(Stage parentStage) {
        Stage stage = new Stage();
        stage.setTitle("Registro de Nuevo Alumno");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER_LEFT);

        // Campos
        TextField txtMatricula = new TextField();
        txtMatricula.setPromptText("Matrícula (Ej: ZS12345678)");

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre");

        TextField txtApellido = new TextField();
        txtApellido.setPromptText("Apellidos");

        // Selección de Materias
        ComboBox<String> cmbMaterias = new ComboBox<>();
        cmbMaterias.getItems().addAll(alumnoDAO.obtenerMateriasDisponibles());
        cmbMaterias.setPromptText("Seleccione Materia");

        TextArea txtMateriasInscritas = new TextArea();
        txtMateriasInscritas.setPromptText("Materias inscritas aparecerán aquí...");
        txtMateriasInscritas.setEditable(false);
        txtMateriasInscritas.setPrefHeight(80);

        Button btnAddMateria = new Button("Agregar Materia");
        btnAddMateria.setOnAction(e -> {
            String selected = cmbMaterias.getValue();
            if (selected != null && !txtMateriasInscritas.getText().contains(selected)) {
                txtMateriasInscritas.appendText(selected + ",");
            }
        });

        // Área para mostrar el QR generado
        Label lblQr = new Label("Código QR Generado:");
        ImageView imgQr = new ImageView();
        imgQr.setFitWidth(150);
        imgQr.setFitHeight(150);

        // Botón Guardar
        Button btnGuardar = new Button("Registrar Alumno");
        btnGuardar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnGuardar.setOnAction(e -> {
            String mat = txtMatricula.getText();
            // Validación básica
            if (!mat.matches("(?i)z[sS]\\d{8}")) {
                mostrarAlerta("Error", "Formato de matrícula inválido (ZS + 8 dígitos).");
                return;
            }

            Alumno nuevoAlumno = new Alumno(
                    mat.toUpperCase(),
                    txtNombre.getText(),
                    txtApellido.getText(),
                    txtMateriasInscritas.getText()
            );

            if (alumnoDAO.registrarAlumno(nuevoAlumno)) {
                mostrarAlerta("Éxito", "Alumno registrado correctamente.");

                // --- AQUÍ LA MAGIA: GENERAR QR AL INSTANTE ---
                // Usamos la utilidad que arreglamos antes
                imgQr.setImage(GeneradorQR.generarQRImagen(mat.toUpperCase(), 200, 200));

            } else {
                mostrarAlerta("Error", "No se pudo registrar (¿Matrícula duplicada?).");
            }
        });

        layout.getChildren().addAll(
                new Label("Matrícula:"), txtMatricula,
                new Label("Datos Personales:"), txtNombre, txtApellido,
                new Label("Inscripción de Materias:"), cmbMaterias, btnAddMateria, txtMateriasInscritas,
                btnGuardar,
                lblQr, imgQr
        );

        Scene scene = new Scene(layout, 400, 600);
        stage.setScene(scene);
        stage.initOwner(parentStage); // Hace que esta ventana dependa de la principal
        stage.show();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}