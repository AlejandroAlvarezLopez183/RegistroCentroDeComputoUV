package mx.uv.db;

import mx.uv.model.MaestroMateria;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MaestroDAO {

    public boolean registrarMaestro(String nombre, String apellido, String materia, String dia, String horaInicio, String horaFin) {
        String sql = "INSERT INTO UsuariosMaestros (Nombre, Apellido, Materia, Horario) VALUES (?, ?, ?, ?)";
        String horarioFormato = dia + ": " + horaInicio + " - " + horaFin;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, apellido);
            pstmt.setString(3, materia);
            pstmt.setString(4, horarioFormato);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- NUEVO: Obtener materias de un profe específico para mostrarlas en la tabla ---
    public ObservableList<MaestroMateria> obtenerMateriasDeMaestro(String nombre, String apellido) {
        ObservableList<MaestroMateria> lista = FXCollections.observableArrayList();
        String sql = "SELECT Materia, Horario FROM UsuariosMaestros WHERE Nombre = ? AND Apellido = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setString(2, apellido);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lista.add(new MaestroMateria(
                        rs.getString("Materia"),
                        rs.getString("Horario")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    // Método de conflicto (el mismo de antes)
    public boolean existeConflicto(String dia, String horaInicio) {
        String busqueda = dia + ": " + horaInicio + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM UsuariosMaestros WHERE Horario LIKE ?")) {
            pstmt.setString(1, "%" + busqueda + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}