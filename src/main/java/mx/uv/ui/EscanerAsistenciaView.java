package mx.uv.ui;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import mx.uv.db.DatabaseConnection;

import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

public class EscanerAsistenciaView {

    private final AtomicBoolean escaneando = new AtomicBoolean(false);
    private Webcam webcam;

    public void mostrar(Stage parentStage) {
        Stage stage = new Stage();
        stage.setTitle("Escáner de Asistencia a Clases");

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #222; -fx-padding: 20;");

        Label lblEstado = new Label("Iniciando cámara...");
        lblEstado.setTextFill(javafx.scene.paint.Color.WHITE);
        lblEstado.setFont(new Font(16));

        ImageView imgView = new ImageView();
        imgView.setFitWidth(640);
        imgView.setFitHeight(480);
        imgView.setPreserveRatio(true);

        Button btnCerrar = new Button("Detener y Cerrar");
        btnCerrar.setOnAction(e -> {
            detenerCamara();
            stage.close();
        });

        root.getChildren().addAll(lblEstado, imgView, btnCerrar);

        // Evento al cerrar la ventana con la X
        stage.setOnCloseRequest(e -> detenerCamara());

        Scene scene = new Scene(root, 700, 600);
        stage.setScene(scene);
        stage.initOwner(parentStage);
        stage.show();

        // Iniciar el hilo de la cámara
        iniciarHiloCamara(imgView, lblEstado);
    }

    private void iniciarHiloCamara(ImageView visor, Label lblEstado) {
        Thread thread = new Thread(() -> {
            webcam = Webcam.getDefault();
            if (webcam != null) {
                webcam.setViewSize(new java.awt.Dimension(640, 480));
                webcam.open();
                escaneando.set(true);

                Platform.runLater(() -> lblEstado.setText("Buscando código QR..."));

                while (escaneando.get()) {
                    if (webcam.isOpen()) {
                        BufferedImage bImage = webcam.getImage();
                        if (bImage != null) {
                            // 1. Convertir imagen para JavaFX
                            WritableImage fxImage = SwingFXUtils.toFXImage(bImage, null);

                            // 2. Actualizar interfaz (siempre dentro de Platform.runLater)
                            Platform.runLater(() -> visor.setImage(fxImage));

                            // 3. Intentar leer QR
                            String resultado = leerQR(bImage);
                            if (resultado != null) {
                                escaneando.set(false); // Pausar escaneo
                                Platform.runLater(() -> procesarMatricula(resultado, lblEstado));
                            }
                        }
                    }
                }
            } else {
                Platform.runLater(() -> lblEstado.setText("Error: No se detectó cámara web."));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String leerQR(BufferedImage imagen) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(imagen);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            return null; // No hay QR en esta imagen
        }
    }

    private void detenerCamara() {
        escaneando.set(false);
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }

    // --- LÓGICA DE NEGOCIO (Validación) ---
    private void procesarMatricula(String rawData, Label lblEstado) {
        // Formato esperado QR: "ZS12345678-1709923..." (Matrícula - Timestamp)
        String[] partes = rawData.split("-");
        String matricula = partes[0];

        // Validar lógica de base de datos
        validarAsistencia(matricula);

        // Reiniciar escaneo después de 3 segundos
        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            escaneando.set(true);
            Platform.runLater(() -> lblEstado.setText("Buscando siguiente alumno..."));
        }).start();
    }

    private void validarAsistencia(String matricula) {
        // Lógica simplificada: Verifica si el alumno existe y registra asistencia
        String sqlCheck = "SELECT Nombre, Apellido, Materias FROM UsuariosAlumnos WHERE Matricula = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlCheck)) {

            pstmt.setString(1, matricula);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String nombre = rs.getString("Nombre");
                String materias = rs.getString("Materias");

                // Aquí podrías añadir la lógica de verificar hora exacta vs horario del maestro
                // Por brevedad, registramos la asistencia directamente.
                registrarEnBD(matricula, nombre, "Clase: " + materias);

                mostrarAlerta("Bienvenido", "Asistencia registrada para: " + nombre);
            } else {
                mostrarAlerta("Error", "Alumno no encontrado en base de datos.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registrarEnBD(String matricula, String nombre, String proposito) {
        String sql = "INSERT INTO AsistenciaCentroDeComputo (Nombre, Apellido, Matricula, Proposito, FechaHora, Asistencia) VALUES (?, ?, ?, ?, ?, 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, "");
            pstmt.setString(3, matricula);
            pstmt.setString(4, proposito);
            pstmt.setString(5, LocalDateTime.now().toString());
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.show(); // No usamos showAndWait para no bloquear el hilo principal demasiado tiempo
    }
}