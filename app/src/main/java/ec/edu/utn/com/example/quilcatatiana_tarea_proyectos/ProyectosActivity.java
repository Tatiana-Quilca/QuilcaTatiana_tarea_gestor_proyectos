package ec.edu.utn.com.example.quilcatatiana_tarea_proyectos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProyectosActivity extends AppCompatActivity {
    private TextView tvProyectoTitle;
    private EditText txtProyectoNombre, txtProyectoDescripcion, txtProyectoFechaInicio, txtProyectoFechaFin;
    private Button btnGuardarProyecto, btnCancelar;
    private Proyectos proyectos;
    private SqlAdmin sqlDb;
    private SessionManager sessionManager;
    private int proyectoId = -1;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proyectos);

        tvProyectoTitle = findViewById(R.id.tvProyectoTitle);
        txtProyectoNombre = findViewById(R.id.txtProyectoNombre);
        txtProyectoDescripcion = findViewById(R.id.txtProyectoDescripcion);
        txtProyectoFechaInicio = findViewById(R.id.txtProyectoFechaInicio);
        txtProyectoFechaFin = findViewById(R.id.txtProyectoFechaFin);
        btnGuardarProyecto = findViewById(R.id.btnGuardarProyecto);
        btnCancelar = findViewById(R.id.btnCancelar);

        sqlDb = new SqlAdmin(this);
        proyectos = new Proyectos(sqlDb);
        sessionManager = new SessionManager(this);

        // Verificar si es edición
        proyectoId = getIntent().getIntExtra("proyecto_id", -1);
        if (proyectoId != -1) {
            tvProyectoTitle.setText("Editar Proyecto");
            Proyecto proyecto = proyectos.Read_ById(proyectoId);
            if (proyecto != null) {
                txtProyectoNombre.setText(proyecto.Nombre);
                txtProyectoDescripcion.setText(proyecto.Descripcion);
                txtProyectoFechaInicio.setText(proyecto.FechaInicio);
                txtProyectoFechaFin.setText(proyecto.FechaFin);
            } else {
                Toast.makeText(this, "Error: Proyecto no encontrado", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        // Configurar DatePicker para las fechas
        txtProyectoFechaInicio.setOnClickListener(v -> mostrarDatePicker(txtProyectoFechaInicio));
        txtProyectoFechaFin.setOnClickListener(v -> mostrarDatePicker(txtProyectoFechaFin));

        // Botón Guardar
        btnGuardarProyecto.setOnClickListener(v -> {
            String nombre = txtProyectoNombre.getText().toString().trim();
            String descripcion = txtProyectoDescripcion.getText().toString().trim();
            String fechaInicio = txtProyectoFechaInicio.getText().toString().trim();
            String fechaFin = txtProyectoFechaFin.getText().toString().trim();
            int usuarioId = sessionManager.getUserId();

            if (nombre.isEmpty() || fechaInicio.isEmpty() || fechaFin.isEmpty()) {
                Toast.makeText(this, "Por favor, complete los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar que la fecha de inicio no sea mayor que la fecha de fin
            if (!validarFechas(fechaInicio, fechaFin)) {
                Toast.makeText(this, "La fecha de inicio no puede ser mayor que la fecha de fin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (proyectoId == -1) {
                // Crear nuevo proyecto
                Proyecto proyecto = proyectos.Create(nombre, descripcion, fechaInicio, fechaFin, usuarioId);
                if (proyecto != null) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Error al crear proyecto", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Editar proyecto existente
                Proyecto proyecto = proyectos.Read_ById(proyectoId);
                if (proyecto != null) {
                    proyecto.Nombre = nombre;
                    proyecto.Descripcion = descripcion;
                    proyecto.FechaInicio = fechaInicio;
                    proyecto.FechaFin = fechaFin;
                    if (proyectos.Update(proyecto)) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Error al actualizar proyecto", Toast.LENGTH_SHORT).show();
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