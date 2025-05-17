package ec.edu.utn.com.example.quilcatatiana_tarea_proyectos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddActividadActivity extends AppCompatActivity {
    private TextView tvActividadTitle;
    private EditText txtActividadNombre, txtActividadDescripcion, txtActividadFechaInicio, txtActividadFechaFin;
    private Spinner spnActividadEstado;
    private Button btnGuardarActividad, btnCancelar;
    private int proyectoId, actividadId = -1;
    private Actividades actividades;
    private Proyectos proyectos;
    private SqlAdmin sqlDb;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_actividad);

        tvActividadTitle = findViewById(R.id.tvActividadTitle);
        txtActividadNombre = findViewById(R.id.txtActividadNombre);
        txtActividadDescripcion = findViewById(R.id.txtActividadDescripcion);
        txtActividadFechaInicio = findViewById(R.id.txtActividadFechaInicio);
        txtActividadFechaFin = findViewById(R.id.txtActividadFechaFin);
        spnActividadEstado = findViewById(R.id.spnActividadEstado);
        btnGuardarActividad = findViewById(R.id.btnGuardarActividad);
        btnCancelar = findViewById(R.id.btnCancelar);

        sqlDb = new SqlAdmin(this);
        proyectos = new Proyectos(sqlDb);
        actividades = new Actividades(sqlDb);
        proyectoId = getIntent().getIntExtra("proyecto_id", -1);
        actividadId = getIntent().getIntExtra("actividad_id", -1);

        if (proyectoId == -1) {
            Toast.makeText(this, "Error: Proyecto no válido", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Configurar el Spinner para los estados
        ArrayAdapter<String> estadoAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Planificado", "En ejecución", "Realizado"});
        estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnActividadEstado.setAdapter(estadoAdapter);

        // Verificar si es edición
        if (actividadId != -1) {
            tvActividadTitle.setText("Editar Actividad");
            Actividad actividad = actividades.Read_ById(actividadId);
            if (actividad != null && actividad.ProyectoId == proyectoId) {
                txtActividadNombre.setText(actividad.Nombre);
                txtActividadDescripcion.setText(actividad.Descripcion);
                txtActividadFechaInicio.setText(actividad.FechaInicio);
                txtActividadFechaFin.setText(actividad.FechaFin);
                spnActividadEstado.setSelection(((ArrayAdapter<String>) spnActividadEstado.getAdapter()).getPosition(actividad.Estado));
            } else {
                Toast.makeText(this, "Error: Actividad no encontrada o no pertenece a este proyecto", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        // Configurar DatePicker para las fechas
        txtActividadFechaInicio.setOnClickListener(v -> mostrarDatePicker(txtActividadFechaInicio));
        txtActividadFechaFin.setOnClickListener(v -> mostrarDatePicker(txtActividadFechaFin));

        // Botón Guardar
        btnGuardarActividad.setOnClickListener(v -> {
            String nombre = txtActividadNombre.getText().toString().trim();
            String descripcion = txtActividadDescripcion.getText().toString().trim();
            String fechaInicio = txtActividadFechaInicio.getText().toString().trim();
            String fechaFin = txtActividadFechaFin.getText().toString().trim();
            String estado = spnActividadEstado.getSelectedItem().toString();

            if (nombre.isEmpty() || fechaInicio.isEmpty() || fechaFin.isEmpty()) {
                Toast.makeText(this, "Por favor, complete los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que la fecha de inicio no sea mayor que la fecha de fin
            if (!validarFechas(fechaInicio, fechaFin)) {
                Toast.makeText(this, "La fecha de inicio no puede ser mayor que la fecha de fin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (actividadId == -1) {
                // Crear nueva actividad
                Actividad actividad = actividades.Create(nombre, descripcion, fechaInicio, fechaFin, estado, proyectoId);
                if (actividad != null) {
                    actividades.recalcularPorcentajeAvance(proyectos, proyectoId);
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Error al crear actividad", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Editar actividad existente
                Actividad actividad = actividades.Read_ById(actividadId);
                if (actividad != null) {
                    actividad.Nombre = nombre;
                    actividad.Descripcion = descripcion;
                    actividad.FechaInicio = fechaInicio;
                    actividad.FechaFin = fechaFin;
                    actividad.Estado = estado;
                    if (actividades.Update(actividad)) {
                        actividades.recalcularPorcentajeAvance(proyectos, proyectoId);
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Error al actualizar actividad", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Botón Cancelar
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void mostrarDatePicker(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    editText.setText(sdf.format(selectedDate.getTime()));
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private boolean validarFechas(String fechaInicio, String fechaFin) {
        try {
            Date inicio = sdf.parse(fechaInicio);
            Date fin = sdf.parse(fechaFin);
            return inicio != null && fin != null && !inicio.after(fin);
        } catch (ParseException e) {
            Toast.makeText(this, "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sqlDb != null) {
            sqlDb.close();
        }
    }
}