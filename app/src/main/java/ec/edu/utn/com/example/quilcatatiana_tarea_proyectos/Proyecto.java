package ec.edu.utn.com.example.quilcatatiana_tarea_proyectos;

public class Proyecto {
    public int Id;
    public String Nombre;
    public String Descripcion;
    public String FechaInicio;
    public String FechaFin;
    public int UsuarioId;
    public float PorcentajeAvance;  // Campo calculado (no almacenado en DB)

    public Proyecto() {
        // Constructor vacío
    }

    public Proyecto(int id, String nombre, String descripcion, String fechaInicio, String fechaFin, int usuarioId) {
        Id = id;
        Nombre = nombre;
        Descripcion = descripcion;
        FechaInicio = fechaInicio;
        FechaFin = fechaFin;
        UsuarioId = usuarioId;
        PorcentajeAvance = 0;
    }
}