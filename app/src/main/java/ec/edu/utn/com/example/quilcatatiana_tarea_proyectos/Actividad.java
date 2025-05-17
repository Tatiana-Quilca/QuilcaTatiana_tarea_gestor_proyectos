package ec.edu.utn.com.example.quilcatatiana_tarea_proyectos;

public class Actividad {
    public int Id;
    public String Nombre;
    public String Descripcion;
    public String FechaInicio;
    public String FechaFin;
    public String Estado;  // "Planificado", "En ejecución", "Realizado"
    public int ProyectoId;

    public Actividad() {
        // Constructor vacío
    }

    public Actividad(int id, String nombre, String descripcion, String fechaInicio, String fechaFin, String estado, int proyectoId) {
        Id = id;
        Nombre = nombre;
        Descripcion = descripcion;
        FechaInicio = fechaInicio;
        FechaFin = fechaFin;
        Estado = estado;
        ProyectoId = proyectoId;
    }
}