package ec.edu.utn.com.example.quilcatatiana_tarea_proyectos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Proyectos {
    private SqlAdmin sqlDb;

    public Proyectos(SqlAdmin sqlAdmin) {
        this.sqlDb = sqlAdmin;
    }

    public Proyecto Create(String nombre, String descripcion, String fechaInicio, String fechaFin, int usuarioId) {
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("descripcion", descripcion);
        values.put("fecha_inicio", fechaInicio);
        values.put("fecha_fin", fechaFin);
        values.put("id_usuario", usuarioId);

        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        long id = dbWriter.insert("proyectos", null, values);
        dbWriter.close();

        if (id <= 0) {
            Log.e("ProyectosApp", "No se pudo insertar el proyecto");
            return null;
        } else {
            Proyecto proyecto = new Proyecto();
            proyecto.Id = (int) id;
            proyecto.Nombre = nombre;
            proyecto.Descripcion = descripcion;
            proyecto.FechaInicio = fechaInicio;
            proyecto.FechaFin = fechaFin;
            proyecto.UsuarioId = usuarioId;
            proyecto.PorcentajeAvance = 0;
            return proyecto;
        }
    }

    public Proyecto Read_ById(int id) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT id_proyecto, nombre, descripcion, fecha_inicio, fecha_fin, id_usuario " +
                "FROM proyectos WHERE id_proyecto = " + id, null);

        if (c.moveToFirst()) {
            Proyecto proyecto = new Proyecto();
            proyecto.Id = c.getInt(0);
            proyecto.Nombre = c.getString(1);
            proyecto.Descripcion = c.getString(2);
            proyecto.FechaInicio = c.getString(3);
            proyecto.FechaFin = c.getString(4);
            proyecto.UsuarioId = c.getInt(5);
            proyecto.PorcentajeAvance = calcularPorcentajeAvance(proyecto.Id);
            c.close();
            dbReader.close();
            return proyecto;
        } else {
            Log.i("ProyectosApp", "No se encontró el proyecto con ID: " + id);
            c.close();
            dbReader.close();
            return null;
        }
    }

    public List<Proyecto> Read_ByUsuarioId(int usuarioId) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT id_proyecto, nombre, descripcion, fecha_inicio, fecha_fin, id_usuario " +
                "FROM proyectos WHERE id_usuario = ? ORDER BY fecha_inicio", new String[]{String.valueOf(usuarioId)});

        List<Proyecto> res = new ArrayList<>();
        while (c.moveToNext()) {
            Proyecto proyecto = new Proyecto();
            proyecto.Id = c.getInt(0);
            proyecto.Nombre = c.getString(1);
            proyecto.Descripcion = c.getString(2);
            proyecto.FechaInicio = c.getString(3);
            proyecto.FechaFin = c.getString(4);
            proyecto.UsuarioId = c.getInt(5);
            proyecto.PorcentajeAvance = calcularPorcentajeAvance(proyecto.Id);
            res.add(proyecto);
        }
        c.close();
        dbReader.close();
        return res;
    }

    public boolean Update(Proyecto proyecto) {
        ContentValues values = new ContentValues();
        values.put("nombre", proyecto.Nombre);
        values.put("descripcion", proyecto.Descripcion);
        values.put("fecha_inicio", proyecto.FechaInicio);
        values.put("fecha_fin", proyecto.FechaFin);

        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        int qty = dbWriter.update("proyectos", values, "id_proyecto = " + proyecto.Id, null);
        dbWriter.close();

        if (qty <= 0) {
            Log.e("ProyectosApp", "No se pudo actualizar el proyecto");
            return false;
        }
        return true;
    }

    public boolean Delete(int id) {
        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        int qty = dbWriter.delete("actividades", "id_proyecto = " + id, null);
        qty += dbWriter.delete("proyectos", "id_proyecto = " + id, null);
        dbWriter.close();

        if (qty <= 0) {
            Log.e("ProyectosApp", "No se pudo eliminar el proyecto");
            return false;
        }
        return true;
    }

    private float calcularPorcentajeAvance(int proyectoId) {
        Actividades actividades = new Actividades(sqlDb); // Instancia temporal para calcular el porcentaje
        int[] conteo = actividades.GetConteoPorEstado(proyectoId);
        int total = conteo[0];
        int realizadas = conteo[3];
        return (total == 0) ? 0 : ((float) realizadas / total) * 100;
    }

    /**
     * Recalcula y actualiza el porcentaje de avance de un proyecto
     * @param proyectoId ID del proyecto a actualizar
     */
    public void recalcularPorcentajeAvance(int proyectoId) {
        float nuevoPorcentaje = calcularPorcentajeAvance(proyectoId);
        Proyecto proyecto = Read_ById(proyectoId);
        if (proyecto != null) {
            proyecto.PorcentajeAvance = nuevoPorcentaje;
        }
    }
}