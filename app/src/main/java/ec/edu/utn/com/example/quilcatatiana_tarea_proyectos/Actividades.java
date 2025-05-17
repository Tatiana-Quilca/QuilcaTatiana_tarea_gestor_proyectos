package ec.edu.utn.com.example.quilcatatiana_tarea_proyectos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class Actividades {
    private SqlAdmin sqlDb;

    public Actividades(SqlAdmin sqlAdmin) {
        sqlDb = sqlAdmin;
    }

    /**
     * Crea una nueva actividad en la base de datos
     * @return el objeto Actividad creado, o null si hubo un error
     */
    public Actividad Create(String nombre, String descripcion, String fechaInicio, String fechaFin, String estado, int proyectoId) {
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("descripcion", descripcion);
        values.put("fecha_inicio", fechaInicio);
        values.put("fecha_fin", fechaFin);
        values.put("estado", estado);
        values.put("id_proyecto", proyectoId);

        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        long id = dbWriter.insert("actividades", null, values);
        dbWriter.close();

        if (id <= 0) {
            Log.e("ProyectosApp", "No se pudo insertar la actividad");
            return null;
        } else {
            Actividad actividad = new Actividad();
            actividad.Id = (int) id;
            actividad.Nombre = nombre;
            actividad.Descripcion = descripcion;
            actividad.FechaInicio = fechaInicio;
            actividad.FechaFin = fechaFin;
            actividad.Estado = estado;
            actividad.ProyectoId = proyectoId;
            return actividad;
        }
    }

    /**
     * Busca una actividad por su ID
     * @return el objeto Actividad encontrado, o null si no existe
     */
    public Actividad Read_ById(int id) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT id_actividad, nombre, descripcion, fecha_inicio, fecha_fin, estado, id_proyecto " +
                "FROM actividades WHERE id_actividad = " + id, null);

        if (c.moveToFirst()) {
            Actividad actividad = new Actividad();
            actividad.Id = c.getInt(0);
            actividad.Nombre = c.getString(1);
            actividad.Descripcion = c.getString(2);
            actividad.FechaInicio = c.getString(3);
            actividad.FechaFin = c.getString(4);
            actividad.Estado = c.getString(5);
            actividad.ProyectoId = c.getInt(6);
            c.close();
            dbReader.close();
            return actividad;
        } else {
            Log.i("ProyectosApp", "No se encontró la actividad con ID: " + id);
            c.close();
            dbReader.close();
            return null;
        }
    }

    /**
     * Obtiene todas las actividades de un proyecto
     * @return Lista de actividades
     */
    public List<Actividad> Read_ByProyectoId(int proyectoId) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT id_actividad, nombre, descripcion, fecha_inicio, fecha_fin, estado, id_proyecto " +
                "FROM actividades WHERE id_proyecto = ? ORDER BY fecha_inicio", new String[]{String.valueOf(proyectoId)});

        List<Actividad> res = new ArrayList<>();
        while (c.moveToNext()) {
            Actividad actividad = new Actividad();
            actividad.Id = c.getInt(0);
            actividad.Nombre = c.getString(1);
            actividad.Descripcion = c.getString(2);
            actividad.FechaInicio = c.getString(3);
            actividad.FechaFin = c.getString(4);
            actividad.Estado = c.getString(5);
            actividad.ProyectoId = c.getInt(6);
            res.add(actividad);
        }
        c.close();
        dbReader.close();
        return res;
    }

    /**
     * Obtiene todas las actividades
     * @return Lista de actividades
     */
    public List<Actividad> Read_All() {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        Cursor c = dbReader.rawQuery("SELECT id_actividad, nombre, descripcion, fecha_inicio, fecha_fin, estado, id_proyecto " +
                "FROM actividades ORDER BY nombre", null);

        List<Actividad> res = new ArrayList<>();
        while (c.moveToNext()) {
            Actividad actividad = new Actividad();
            actividad.Id = c.getInt(0);
            actividad.Nombre = c.getString(1);
            actividad.Descripcion = c.getString(2);
            actividad.FechaInicio = c.getString(3);
            actividad.FechaFin = c.getString(4);
            actividad.Estado = c.getString(5);
            actividad.ProyectoId = c.getInt(6);
            res.add(actividad);
        }
        c.close();
        dbReader.close();
        return res;
    }

    /**
     * Actualiza los datos de una actividad
     * @return true si se actualizó correctamente, false si hubo un error
     */
    public boolean Update(Actividad actividad) {
        ContentValues values = new ContentValues();
        values.put("nombre", actividad.Nombre);
        values.put("descripcion", actividad.Descripcion);
        values.put("fecha_inicio", actividad.FechaInicio);
        values.put("fecha_fin", actividad.FechaFin);
        values.put("estado", actividad.Estado);
        values.put("id_proyecto", actividad.ProyectoId);

        SQLiteDatabase dbWriter = sqlDb.getWritableDatabase();
        int qty = dbWriter.update("actividades", values, "id_actividad = " + actividad.Id, null);
        dbWriter.close();

        if (qty > 0) {
            return true;
        } else {
            Log.e("ProyectosApp", "No se pudo actualizar la actividad");
            return false;
        }
    }

    /**
     * Elimina una actividad por su ID
     * @return true si se eliminó correctamente, false si hubo un error
     */
    public boolean Delete(int id) {
        SQLiteDatabase db = sqlDb.getWritableDatabase();
        boolean success = false;
        try {
            db.beginTransaction();
            int rowsDeleted = db.delete("Actividades", "id_actividad= ?", new String[]{String.valueOf(id)});
            if (rowsDeleted > 0) {
                db.setTransactionSuccessful();
                success = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                db.endTransaction();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return success;
    }
    /**
     * Obtiene el conteo de actividades por estado para un proyecto
     * @return array con [total, planificadas, en_ejecucion, realizadas]
     */
    public int[] GetConteoPorEstado(int proyectoId) {
        SQLiteDatabase dbReader = sqlDb.getReadableDatabase();
        int[] conteo = new int[4]; // [total, planificadas, en_ejecucion, realizadas]

        // Total de actividades
        Cursor cTotal = dbReader.rawQuery("SELECT COUNT(*) FROM actividades WHERE id_proyecto = ?",
                new String[]{String.valueOf(proyectoId)});
        if (cTotal.moveToFirst()) {
            conteo[0] = cTotal.getInt(0);
        }
        cTotal.close();

        // Actividades planificadas
        Cursor cPlanificadas = dbReader.rawQuery("SELECT COUNT(*) FROM actividades WHERE id_proyecto = ? AND estado = ?",
                new String[]{String.valueOf(proyectoId), "Planificado"});
        if (cPlanificadas.moveToFirst()) {
            conteo[1] = cPlanificadas.getInt(0);
        }
        cPlanificadas.close();

        // Actividades en ejecución
        Cursor cEnEjecucion = dbReader.rawQuery("SELECT COUNT(*) FROM actividades WHERE id_proyecto = ? AND estado = ?",
                new String[]{String.valueOf(proyectoId), "En ejecución"});
        if (cEnEjecucion.moveToFirst()) {
            conteo[2] = cEnEjecucion.getInt(0);
        }
        cEnEjecucion.close();

        // Actividades realizadas
        Cursor cRealizadas = dbReader.rawQuery("SELECT COUNT(*) FROM actividades WHERE id_proyecto = ? AND estado = ?",
                new String[]{String.valueOf(proyectoId), "Realizado"});
        if (cRealizadas.moveToFirst()) {
            conteo[3] = cRealizadas.getInt(0);
        }
        cRealizadas.close();

        dbReader.close();
        return conteo;
    }

    /**
     * Recalcula el porcentaje de avance del proyecto asociado a una actividad
     * @param proyectos Instancia de Proyectos para actualizar el porcentaje
     * @param proyectoId ID del proyecto a recalcular
     */
    public void recalcularPorcentajeAvance(Proyectos proyectos, int proyectoId) {
        int[] conteo = GetConteoPorEstado(proyectoId);
        int total = conteo[0];
        int realizadas = conteo[3];
        float porcentaje = (total == 0) ? 0 : ((float) realizadas / total) * 100;
        Proyecto proyecto = proyectos.Read_ById(proyectoId);
        if (proyecto != null) {
            proyecto.PorcentajeAvance = porcentaje;
        }
    }
}