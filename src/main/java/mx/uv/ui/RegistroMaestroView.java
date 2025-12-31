package mx.uv.ui;

import mx.uv.model.MaestroMateria;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import mx.uv.db.MaestroDAO;

import java.util.ArrayList;
import java.util.List;

public class RegistroMaestroView {

    private final MaestroDAO maestroDAO = new MaestroDAO();
    private TableView<MaestroMateria> tablaMaterias;

    // Checkboxes globales para poder limpiarlos después
    private CheckBox cbLunes, cbMartes, cbMiercoles, cbJueves, cbViernes;

    public void mostrar(Stage parentStage) {
        Stage stage = new Stage();
        stage.setTitle("Gestión Académica - Maestros");

        // --- Layout Principal ---
        HBox root = new HBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f4f4;");

        // --- Panel Izquierdo (Formulario) ---
        VBox formPanel = new VBox(15);
        formPanel.setPadding(new Insets(20));
        formPanel.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0); -fx-background-radius: 10;");
        formPanel.setPrefWidth(380); // Un poco más ancho para los checks

        Label lblTitulo = new Label("Nueva Cátedra");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(Color.web("#003366"));

        // Campos
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del Docente");

        TextField txtApellido = new TextField();
        txtApellido.setPromptText("Apellidos");

        Separator sep = new Separator();

        TextField txtMateria = new TextField();
        txtMateria.setPromptText("Nombre de la Materia");
        txtMateria.setStyle("-fx-font-weight: bold;");

        // --- Días (Checkboxes) ---
        Label lblDias = new Label("Seleccione los días de clase:");

        cbLunes = new CheckBox("Lunes");
        cbMartes = new CheckBox("Martes");
        cbMiercoles = new CheckBox("Miércoles");
        cbJueves = new CheckBox("Jueves");
        cbViernes = new CheckBox("Viernes");

        // Organizarlos en una rejilla pequeña (FlowPane)
        FlowPane panelDias = new FlowPane(10, 10);
        panelDias.getChildren().addAll(cbLunes, cbMartes, cbMiercoles, cbJueves, cbViernes);

        // --- Horarios ---
        HBox horasBox = new HBox(10);
        ComboBox<String> cmbInicio = new ComboBox<>();
        ComboBox<String> cmbFin = new ComboBox<>();
        cargarHoras(cmbInicio, cmbFin);

        cmbInicio.setPromptText("Inicio");
        cmbFin.setPromptText("Fin");
        horasBox.getChildren().addAll(new Label("De:"), cmbInicio, new Label("A:"), cmbFin);
        horasBox.setAlignment(Pos.CENTER_LEFT);

        // Botón Guardar
        Button btnGuardar = new Button("Registrar Materia");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setStyle("-fx-background-color: #005bb5; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 5;");

        formPanel.getChildren().addAll(
                lblTitulo,
                new Label("Datos del Docente"), txtNombre, txtApellido,
                sep,
                new Label("Asignatura"), txtMateria,
                lblDias, panelDias, // Agregamos los checkboxes
                new Label("Horario"), horasBox,
                new Region() {{ setPrefHeight(10); }},
                btnGuardar
        );

        // --- Panel Derecho (Tabla Visual) ---
        VBox tablePanel = new VBox(10);
        HBox.setHgrow(tablePanel, Priority.ALWAYS);

        Label lblLista = new Label("Materias Asignadas a este Docente");
        lblLista.setFont(Font.font("Arial", 14));

        tablaMaterias = new TableView<>();
        TableColumn<MaestroMateria, String> colMat = new TableColumn<>("Materia");
        colMat.setCellValueFactory(new PropertyValueFactory<>("materia"));

        TableColumn<MaestroMateria, String> colHor = new TableColumn<>("Horario Asignado");
        colHor.setCellValueFactory(new PropertyValueFactory<>("horario"));

        tablaMaterias.getColumns().addAll(colMat, colHor);
        tablaMaterias.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaMaterias.setPlaceholder(new Label("Ingrese nombre para ver materias"));

        tablePanel.getChildren().addAll(lblLista, tablaMaterias);

        // --- Lógica ---
        txtApellido.setOnKeyReleased(e -> actualizarTabla(txtNombre.getText(), txtApellido.getText()));

        btnGuardar.setOnAction(e -> {
            // 1. Obtener días seleccionados
            List<String> diasSeleccionados = obtenerDiasSeleccionados();

            if (validar(txtNombre, txtApellido, txtMateria, diasSeleccionados, cmbInicio, cmbFin)) {

                int guardados = 0;
                StringBuilder errores = new StringBuilder();

                // 2. Bucle: Intentar guardar para cada día seleccionado
                for (String dia : diasSeleccionados) {

                    if (maestroDAO.existeConflicto(dia, cmbInicio.getValue())) {
                        errores.append("Choque en ").append(dia).append("\n");
                        continue; // Saltamos este día, pero intentamos los otros
                    }

                    boolean exito = maestroDAO.registrarMaestro(
                            txtNombre.getText(), txtApellido.getText(),
                            txtMateria.getText(), dia,
                            cmbInicio.getValue(), cmbFin.getValue()
                    );

                    if (exito) guardados++;
                }

                // 3. Feedback al usuario
                if (guardados > 0) {
                    String msg = "Se registraron " + guardados + " clases exitosamente.";
                    if (errores.length() > 0) msg += "\n\nNo se pudieron registrar:\n" + errores.toString();

                    alerta(Alert.AlertType.INFORMATION, "Resultado", msg);
                    actualizarTabla(txtNombre.getText(), txtApellido.getText());

                    // Limpiar formulario parcial (dejamos nombre/apellido)
                    txtMateria.clear();
                    limpiarCheckboxes();
                } else {
                    alerta(Alert.AlertType.ERROR, "Error", "No se pudo registrar ninguna clase.\n" + errores.toString());
                }
            }
        });

        root.getChildren().addAll(formPanel, tablePanel);

        Scene scene = new Scene(root, 900, 550);
        stage.setScene(scene);
        stage.initOwner(parentStage);
        stage.show();
    }

    // Método auxiliar para saber qué casillas marcó el usuario
    private List<String> obtenerDiasSeleccionados() {
        List<String> dias = new ArrayList<>();
        if (cbLunes.isSelected()) dias.add("Lunes");
        if (cbMartes.isSelected()) dias.add("Martes");
        if (cbMiercoles.isSelected()) dias.add("Miércoles");
        if (cbJueves.isSelected()) dias.add("Jueves");
        if (cbViernes.isSelected()) dias.add("Viernes");
        return dias;
    }

    private void limpiarCheckboxes() {
        cbLunes.setSelected(false);
        cbMartes.setSelected(false);
        cbMiercoles.setSelected(false);
        cbJueves.setSelected(false);
        cbViernes.setSelected(false);
    }

    private void cargarHoras(ComboBox<String> ini, ComboBox<String> fin) {
        for (int i = 7; i <= 21; i++) {
            String hora = String.format("%02d:00", i);
            ini.getItems().add(hora);
            fin.getItems().add(hora);
        }
    }

    private void actualizarTabla(String nom, String ape) {
        if (!nom.isEmpty() && !ape.isEmpty()) {
            tablaMaterias.setItems(maestroDAO.obtenerMateriasDeMaestro(nom, ape));
        }
    }

    private boolean validar(TextField n, TextField a, TextField m, List<String> dias, ComboBox i, ComboBox f) {
        if (n.getText().isEmpty() || a.getText().isEmpty() || m.getText().isEmpty() ||
                dias.isEmpty() || i.getValue() == null || f.getValue() == null) {
            alerta(Alert.AlertType.WARNING, "Faltan datos", "Verifique campos vacíos o seleccione al menos un día.");
            return false;
        }
        return true;
    }

    private void alerta(Alert.AlertType tipo, String titulo, String msg) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}