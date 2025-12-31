package mx.uv.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.time.Instant;

public class GeneradorQR {

    /**
     * Genera un código QR directamente como una Imagen de JavaFX.
     * No requiere guardar archivos ni usar servidores.
     *
     * @param matricula El texto a codificar (la matrícula)
     * @param ancho Ancho en píxeles
     * @param alto Alto en píxeles
     * @return Objeto Image listo para poner en un ImageView
     */
    public static Image generarQRImagen(String matricula, int ancho, int alto) {
        try {
            // Generamos el contenido dinámico (Matrícula + Timestamp para seguridad)
            // Esto evita que un alumno tome captura y la use otro día.
            long intervalo = Instant.now().getEpochSecond() / 10; // Cambia cada 10 segs
            String contenido = matricula + "-" + intervalo;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(contenido, BarcodeFormat.QR_CODE, ancho, alto);

            // Convertimos la matriz de bits de ZXing a una Imagen de JavaFX
            WritableImage image = new WritableImage(ancho, alto);
            PixelWriter pw = image.getPixelWriter();

            for (int x = 0; x < ancho; x++) {
                for (int y = 0; y < alto; y++) {
                    // Si el bit es true, pintamos negro, si no, blanco (o transparente)
                    pw.setColor(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return image;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }
}