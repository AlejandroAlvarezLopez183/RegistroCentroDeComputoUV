package mx.uv.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import mx.uv.db.DatabaseConnection;
import mx.uv.model.Asistencia;
import mx.uv.model.Alumno; // Asegúrate de tener este modelo del paso anterior

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

public class AdminPanelView {

    public void mostrar(Stage parentStage) {
        // 1. Pedir contraseña primero
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Acceso Administrativo");
        dialog.setHeaderText("Panel de Control");
        dialog.setContentText("Ingrese contraseña:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && result.get().equals("admin123")) {
            iniciarPanel(parentStage);
        } else {
            new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta").show();
        }
    }

    private void iniciarPanel(Stage parentStage) {
        Stage stage = new Stage();
        stage.setTitle("Panel de Administración");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // --- Pestaña 1: Alumnos ---
        Tab tabAlumnos = new Tab("Alumnos", crearTablaAlumnos());

        // --- Pestaña 2: Reporte Asistencias ---
        Tab tabAsistencias = new Tab("Reporte Asistencias", crearTablaAsistencias());

        tabPane.getTabs().addAll(tabAlumnos, tabAsistencias);

        Scene scene = new Scene(tabPane, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    private TableView<Alumno> crearTablaAlumnos() {
        TableView<Alumno> tabla = new TableView<>();

        // Definir columnas
        TableColumn<Alumno, String> colMat = new TableColumn<>("Matrícula");
        colMat.setCellValueFactory(new PropertyValueFactory<>("matricula"));

        TableColumn<Alumno, String> colNom = new TableColumn<>("Nombre");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Alumno, String> colMatList = new TableColumn<>("Materias");
        colMatList.setCellValueFactory(new PropertyValueFactory<>("materias"));

        tabla.getColumns().addAll(colMat, colNom, colMatList);

        // Cargar datos
        cargarAlumnos(tabla);
        return tabla;
    }

    private TableView<Asistencia> crearTablaAsistencias() {
        TableView<Asistencia> tabla = new TableView<>();

        TableColumn<Asistencia, String> colMat = new TableColumn<>("Matrícula");
        colMat.setCellValueFactory(new PropertyValueFactory<>("matricula"));

        TableColumn<Asistencia, String> colNom = new TableColumn<>("Nombre");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<Asistencia, String> colFecha = new TableColumn<>("Fecha/Hora");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        TableColumn<Asistencia, String> colProp = new TableColumn<>("Propósito");
        colProp.setCellValueFactory(new PropertyValueFactory<>("proposito"));

        tabla.getColumns().addAll(colMat, colNom, colFecha, colProp);

        cargarAsistencias(tabla);
        return tabla;
    }

    // --- Métodos de Carga de Datos ---
    private void cargarAlumnos(TableView<Alumno> tabla) {
        ObservableList<Alumno> lista = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM UsuariosAlumnos")) {

            while (rs.next()) {
                lista.add(new Alumno(
                        rs.getString("Matricula"),
                        rs.getString("Nombre"),
                        rs.getString("Apellido"),
                        rs.getString("Materias")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        tabla.setItems(lista);
    }

    private void cargarAsistencias(TableView<Asistencia> tabla) {
        ObservableList<Asistencia> lista = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM AsistenciaCentroDeComputo ORDER BY FechaHora DESC")) {

            while (rs.next()) {
                lista.add(new Asistencia(
                        rs.getString("Nombre"),
                        rs.getString("Matricula"),
                        rs.getString("FechaHora"),
                        rs.getString("Proposito")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        tabla.setItems(lista);
    }
}