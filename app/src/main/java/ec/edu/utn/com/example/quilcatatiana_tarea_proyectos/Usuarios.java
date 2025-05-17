package ec.edu.utn.com.example.quilcatatiana_tarea_proyectos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Usuarios {
    public SqlAdmin sqlDb;

    public Usuarios(SqlAdmin sqlAdmin) {
        sqlDb = sqlAdmin;
    }

    public Usuario Create(String username, String password, String email) {
        ContentValues values = new ContentValues();
        values.put("nombre_usuario", username);
        values.put("contrasena", password);
        values.put("email", email);
        values.put("created_at", getCurrentDatetime());

        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        long id = dbWriter.insert("usuarios", null, values);
        dbWriter.close();

        if (id <= 0) {
            Log.e("ProyectosApp", "No se pudo insertar el usuario");
            return null;
        } else {
            Usuario usuario = new Usuario();
            usuario.Id = (int) id;
            usuario.Username = username;
            usuario.Password = password;
            usuario.Email = email;
            usuario.CreatedAt = getCurrentDatetime();
            return usuario;
        }
    }

    public Usuario Read_ById(int id) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT id_usuario, nombre_usuario, contrasena, email, created_at FROM usuarios WHERE id_usuario = " + id, null);

        if (c.moveToFirst()) {
            Usuario usuario = new Usuario();
            usuario.Id = c.getInt(0);
            usuario.Username = c.getString(1);
            usuario.Password = c.getString(2);
            usuario.Email = c.getString(3);
            usuario.CreatedAt = c.getString(4);
            c.close();
            dbReader.close();
            return usuario;
        } else {
            Log.i("ProyectosApp", "No se encontró el usuario con ID: " + id);
            c.close();
            dbReader.close();
            return null;
        }
    }

    public Usuario Login(String username, String password) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT id_usuario, nombre_usuario, contrasena, email, created_at FROM usuarios " +
                "WHERE nombre_usuario = ? AND contrasena = ?", new String[]{username, password});

        if (c.moveToFirst()) {
            Usuario usuario = new Usuario();
            usuario.Id = c.getInt(0);
            usuario.Username = c.getString(1);
            usuario.Password = c.getString(2);
            usuario.Email = c.getString(3);
            usuario.CreatedAt = c.getString(4);
            c.close();
            dbReader.close();
            return usuario;
        } else {
            Log.i("ProyectosApp", "Credenciales incorrectas para: " + username);
            c.close();
            dbReader.close();
            return null;
        }
    }

    public List<Usuario> Read_All() {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT id_usuario, nombre_usuario, contrasena, email, created_at FROM usuarios ORDER BY nombre_usuario", null);

        List<Usuario> res = new ArrayList<>();
        while (c.moveToNext()) {
            Usuario usuario = new Usuario();
            usuario.Id = c.getInt(0);
            usuario.Username = c.getString(1);
            usuario.Password = c.getString(2);
            usuario.Email = c.getString(3);
            usuario.CreatedAt = c.getString(4);
            res.add(usuario);
        }
        c.close();
        dbReader.close();
        return res;
    }

    public boolean Update(Usuario usuario) {
        ContentValues values = new ContentValues();
        values.put("nombre_usuario", usuario.Username);
        values.put("contrasena", usuario.Password);
        values.put("email", usuario.Email);

        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        int qty = dbWriter.update("usuarios", values, "id_usuario = " + usuario.Id, null);
        dbWriter.close();

        if (qty <= 0) {
            Log.e("ProyectosApp", "No se pudo actualizar el usuario");
            return false;
        }
        return true;
    }

    public boolean Delete(int id) {
        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        int qty = dbWriter.delete("usuarios", "id_usuario = " + id, null);
        dbWriter.close();

        if (qty <= 0) {
            Log.e("ProyectosApp", "No se pudo eliminar el usuario");
            return false;
        }
        return true;
    }

    public boolean ExisteUsername(String username) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT id_usuario FROM usuarios WHERE nombre_usuario = ?", new String[]{username});

        boolean existe = c.getCount() > 0;
        c.close();
        dbReader.close();
        return existe;
    }

    public boolean ExisteEmail(String email) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT id_usuario FROM usuarios WHERE email = ?", new String[]{email});

        boolean existe = c.getCount() > 0;
        c.close();
        dbReader.close();
        return existe;
    }

    private String getCurrentDatetime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    public Usuario RecoverPassword(String email) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT id_usuario, nombre_usuario, contrasena, email, created_at FROM usuarios WHERE email = ?",
                new String[]{email});

        Usuario usuario = null;
        if (c != null && c.moveToFirst()) {
            try {
                usuario = new Usuario();
                usuario.Id = c.getInt(c.getColumnIndexOrThrow("id_usuario"));
                usuario.Username = c.getString(c.getColumnIndexOrThrow("nombre_usuario"));
                usuario.Password = c.getString(c.getColumnIndexOrThrow("contrasena"));
                usuario.Email = c.getString(c.getColumnIndexOrThrow("email"));
                usuario.CreatedAt = c.getString(c.getColumnIndexOrThrow("created_at"));
            } catch (IllegalArgumentException e) {
                Log.e("ProyectosApp", "Error al leer columnas: " + e.getMessage());
                usuario = null;
            } finally {
                c.close();
            }
        }
        if (c != null) {
            c.close();
        }
        dbReader.close();
        return usuario;
    }

    public boolean UpdatePassword(int userId, String newPassword) {
        ContentValues values = new ContentValues();
        values.put("contrasena", newPassword);

        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        int qty = dbWriter.update("usuarios", values, "id_usuario = ?", new String[]{String.valueOf(userId)});
        dbWriter.close();

        if (qty <= 0) {
            Log.e("ProyectosApp", "No se pudo actualizar la contraseña para el usuario con ID: " + userId);
            return false;
        }
        return true;
    }
}