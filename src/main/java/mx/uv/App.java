package mx.uv;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import mx.uv.ui.*;

import java.io.InputStream;

public class App extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Sistema de Control - Centro de Cómputo");

        // Crear la escena principal
        Scene mainScene = createMainScene();

        stage.setScene(mainScene);
        stage.setMaximized(true); // Pantalla completa por defecto
        stage.show();
    }

    private Scene createMainScene() {
        // --- Panel Izquierdo (Imagen) ---
        StackPane leftPane = new StackPane();
        // Nota: Asegúrate de mover tus imágenes a la carpeta 'src/main/resources/images/'
        try {
            InputStream is = getClass().getResourceAsStream("/images/centro.jpg");
            if (is != null) {
                ImageView bgImage = new ImageView(new Image(is));
                bgImage.fitWidthProperty().bind(leftPane.widthProperty());
                bgImage.fitHeightProperty().bind(leftPane.heightProperty());
                leftPane.getChildren().add(bgImage);
            }
        } catch (Exception e) {
            leftPane.setStyle("-fx-background-color: #333;"); // Fallback si no hay imagen
        }

        // --- Panel Derecho (Controles) ---
        VBox rightPane = new VBox(20);
        rightPane.setPadding(new Insets(40));
        rightPane.setAlignment(Pos.CENTER);
        rightPane.setStyle("-fx-background-color: white;");

        // Título
        Label title = new Label("Centro de Cómputo UV");
        title.setFont(new Font("Arial", 24));
        title.setTextFill(Color.web("#003366"));

        // Botones
        Button btnInscripcion = createStyledButton("Inscripción (Alumnos)");
        Button btnMaestros = createStyledButton("Gestión de Maestros"); // NUEVO
        Button btnRegistroGeneral = createStyledButton("Registro Uso General"); // NUEVO
        Button btnScanner = createStyledButton("Escaner Entrada a Clase");
        Button btnAdmin = createStyledButton("Panel Administrador");

        // Acciones de Botones (Aquí conectaremos las otras ventanas luego)
        btnInscripcion.setOnAction(e -> new RegistroAlumnoView().mostrar(primaryStage));
        btnMaestros.setOnAction(e -> new RegistroMaestroView().mostrar(primaryStage)); // CONECTADO
        btnRegistroGeneral.setOnAction(e -> new RegistroGeneralView().mostrar(primaryStage)); // CONECTADO
        btnScanner.setOnAction(e -> new EscanerAsistenciaView().mostrar(primaryStage));
        btnAdmin.setOnAction(e -> new AdminPanelView().mostrar(primaryStage));

        // Agregar todo al panel derecho
        rightPane.getChildren().addAll(title, btnInscripcion,btnMaestros, btnRegistroGeneral, btnScanner, btnAdmin);
        // --- Layout Principal (SplitPane o GridPane) ---
        GridPane root = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);

        root.getColumnConstraints().addAll(col1, col2);
        root.add(leftPane, 0, 0);
        root.add(rightPane, 1, 0);

        // Ajustar altura
        RowConstraints row = new RowConstraints();
        row.setPercentHeight(100);
        root.getRowConstraints().add(row);

        return new Scene(root, 1000, 600);
    }

    private Button createStyledButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(45);
        btn.setStyle("-fx-background-color: #0066cc; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;");
        // Efecto Hover
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #005bb5; -fx-text-fill: white; -fx-font-size: 14px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #0066cc; -fx-text-fill: white; -fx-font-size: 14px;"));
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}