package ec.edu.utn.com.example.quilcatatiana_tarea_proyectos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SqlAdmin extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "GestorProyectos.db";
    private static final int DATABASE_VERSION = 2;

    public SqlAdmin(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS usuarios (id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, nombre_usuario TEXT UNIQUE, contrasena TEXT, email TEXT, created_at TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS proyectos (id_proyecto INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, descripcion TEXT, fecha_inicio TEXT, fecha_fin TEXT, id_usuario INTEGER, CONSTRAINT fk_usuarios FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario))");
        db.execSQL("CREATE TABLE IF NOT EXISTS actividades (id_actividad INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, descripcion TEXT, fecha_inicio TEXT, fecha_fin TEXT, estado TEXT, id_proyecto INTEGER, CONSTRAINT fk_proyectos FOREIGN KEY (id_proyecto) REFERENCES proyectos(id_proyecto))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS actividades");
        db.execSQL("DROP TABLE IF EXISTS proyectos");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }
}