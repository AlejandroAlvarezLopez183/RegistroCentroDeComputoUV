package mx.uv.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:CentroComputo.db"; // Archivo en la raíz del proyecto
    private static Connection instance = null;

    // Método para obtener la única instancia de conexión
    public static Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = DriverManager.getConnection(URL);
            inicializarTablas(instance);
        }
        return instance;
    }

    private static void inicializarTablas(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Tabla Alumnos
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS UsuariosAlumnos (
                    Matricula TEXT PRIMARY KEY,
                    Nombre TEXT NOT NULL,
                    Apellido TEXT NOT NULL,
                    Materias TEXT NOT NULL
                );
            """);

            // Tabla Maestros
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS UsuariosMaestros (
                    IDMaestro INTEGER PRIMARY KEY AUTOINCREMENT,
                    Nombre TEXT NOT NULL,
                    Apellido TEXT NOT NULL,
                    Materia TEXT NOT NULL,
                    Horario TEXT NOT NULL
                );
            """);

            // Tabla Asistencias
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS AsistenciaCentroDeComputo (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    Nombre TEXT NOT NULL,
                    Apellido TEXT NOT NULL,
                    Carrera TEXT,
                    Matricula TEXT NOT NULL,
                    Proposito TEXT,
                    Horas INTEGER DEFAULT 0,
                    FechaHora TEXT DEFAULT (datetime('now', 'localtime')),
                    Asistencia INTEGER DEFAULT 0
                );
            """);
        }
    }
}