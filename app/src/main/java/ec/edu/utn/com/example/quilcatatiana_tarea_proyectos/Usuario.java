package ec.edu.utn.com.example.quilcatatiana_tarea_proyectos;

public class Usuario {
    public int Id;
    public String Username;
    public String Password;
    public String Email;
    public String CreatedAt;

    public Usuario() {
        // Constructor vacío
    }

    public Usuario(int id, String username, String password, String email, String createdAt) {
        Id = id;
        Username = username;
        Password = password;
        Email = email;
        CreatedAt = createdAt;
    }
}