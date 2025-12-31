package mx.uv.model;

public class Alumno {
    private String matricula;
    private String nombre;
    private String apellido;
    private String materias; // Podríamos mejorar esto a List<String> luego

    public Alumno(String matricula, String nombre, String apellido, String materias) {
        this.matricula = matricula;
        this.nombre = nombre;
        this.apellido = apellido;
        this.materias = materias;
    }

    // Getters y Setters (Necesarios para las tablas de JavaFX)
    public String getMatricula() { return matricula; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getMaterias() { return materias; }

    // toString para depuración
    @Override
    public String toString() { return nombre + " " + apellido; }
}