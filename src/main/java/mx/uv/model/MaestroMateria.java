package mx.uv.model;

public class MaestroMateria {
    private String materia;
    private String horario;

    public MaestroMateria(String materia, String horario) {
        this.materia = materia;
        this.horario = horario;
    }

    public String getMateria() { return materia; }
    public String getHorario() { return horario; }
}