package mx.uv.model;

public class Asistencia {
    private String nombre;
    private String matricula;
    private String fecha;
    private String proposito;

    public Asistencia(String nombre, String matricula, String fecha, String proposito) {
        this.nombre = nombre;
        this.matricula = matricula;
        this.fecha = fecha;
        this.proposito = proposito;
    }

    // Getters obligatorios para TableView
    public String getNombre() { return nombre; }
    public String getMatricula() { return matricula; }
    public String getFecha() { return fecha; }
    public String getProposito() { return proposito; }
}