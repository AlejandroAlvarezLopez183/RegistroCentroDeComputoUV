package mx.uv.db;

import mx.uv.model.Alumno;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlumnoDAO {

    public boolean registrarAlumno(Alumno alumno) {
        String sql = "INSERT INTO UsuariosAlumnos (Matricula, Nombre, Apellido, Materias) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, alumno.getMatricula());
            pstmt.setString(2, alumno.getNombre());
            pstmt.setString(3, alumno.getApellido());
            pstmt.setString(4, alumno.getMaterias());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar alumno: " + e.getMessage());
            return false;
        }
    }

    public List<String> obtenerMateriasDisponibles() {
        List<String> materias = new ArrayList<>();
        String sql = "SELECT DISTINCT Materia FROM UsuariosMaestros";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                materias.add(rs.getString("Materia"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return materias;
    }

    // Aquí puedes añadir métodos para buscar, borrar, etc.
}