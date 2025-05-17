package ec.edu.utn.com.example.quilcatatiana_tarea_proyectos;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ActividadesActivity extends AppCompatActivity {
    private TextView tvActividadesTitle;
    private ListView lvActividades;
    private FloatingActionButton fabAddActividad;
    private ImageButton btnVolver;
    private SearchView searchViewActividades;
    private int proyectoId;
    private Proyectos proyectos;
    private Actividades actividades;
    private SqlAdmin sqlDb;
    private ActividadAdapter actividadAdapter;
    private List<Actividad> listaActividades;
    private ActivityResultLauncher<Intent> actividadLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividades);

        tvActividadesTitle = findViewById(R.id.tvActividadesTitle);
        lvActividades = findViewById(R.id.lvActividades);
        fabAddActividad = findViewById(R.id.fabAddActividad);
        btnVolver = findViewById(R.id.btnVolver);
        searchViewActividades = findViewById(R.id.searchViewActividades);

        sqlDb = new SqlAdmin(this);
        proyectos = new Proyectos(sqlDb);
        actividades = new Actividades(sqlDb);
        proyectoId = getIntent().getIntExtra("proyecto_id", -1);

        if (proyectoId == -1) {
            Toast.makeText(this, "Error: Proyecto no válido", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        listaActividades = new ArrayList<>();
        actividadAdapter = new ActividadAdapter(this, listaActividades);
        lvActividades.setAdapter(actividadAdapter);

        // Configurar el launcher para agregar/editar actividades
        actividadLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        cargarActividades();
                        // Notificar a MainActivity para recargar proyectos
                        setResult(RESULT_OK);
                    }
                });

        // Clic simple: Editar actividad (mantenemos esto, pero ahora también tendremos el ícono)
        lvActividades.setOnItemClickListener((parent, view, position, id) -> {
            Actividad actividad = listaActividades.get(position);
            Intent intent = new Intent(ActividadesActivity.this, AddActividadActivity.class);
            intent.putExtra("proyecto_id", proyectoId);
            intent.putExtra("actividad_id", actividad.Id);
            actividadLauncher.launch(intent);
        });

        // Configurar SearchView para filtrar actividades
        searchViewActividades.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarActividades(newText);
                return true;
            }
        });

        cargarActividades();

        fabAddActividad.setOnClickListener(v -> {
            Intent intent = new Intent(ActividadesActivity.this, AddActividadActivity.class);
            intent.putExtra("proyecto_id", proyectoId);
            actividadLauncher.launch(intent);
        });

        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarActividades() {
        listaActividades = actividades.Read_ByProyectoId(proyectoId);
        if (listaActividades.isEmpty()) {
            lvActividades.setVisibility(View.GONE);
        } else {
            lvActividades.setVisibility(View.VISIBLE);
        }
        actividadAdapter.clear();
        actividadAdapter.addAll(listaActividades);
        actividadAdapter.notifyDataSetChanged();
    }

    private void filtrarActividades(String query) {
        List<Actividad> filteredActividades = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filteredActividades.addAll(actividades.Read_ByProyectoId(proyectoId));
        } else {
            for (Actividad actividad : listaActividades) {
                if (actividad.Nombre.toLowerCase().contains(query.toLowerCase())) {
                    filteredActividades.add(actividad);
                }
            }
        }
        listaActividades.clear();
        listaActividades.addAll(filteredActividades);
        actividadAdapter.clear();
        actividadAdapter.addAll(filteredActividades);
        actividadAdapter.notifyDataSetChanged();

        if (filteredActividades.isEmpty()) {
            lvActividades.setVisibility(View.GONE);
        } else {
            lvActividades.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // No cerramos sqlDb aquí para evitar problemas si otras actividades lo necesitan
        // Si necesitas cerrar la base de datos, asegúrate de que no se use después
        // if (sqlDb != null) {
        //     sqlDb.close();
        // }
    }

    // Clase adaptador personalizado
    private class ActividadAdapter extends ArrayAdapter<Actividad> {
        private final LayoutInflater inflater;

        public ActividadAdapter(ActividadesActivity context, List<Actividad> actividades) {
            super(context, 0, actividades);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_actividad, parent, false);
            }

            Actividad actividad = getItem(position);
            TextView tvActividadNombre = convertView.findViewById(R.id.tvActividadNombre);
            ImageView ivEditar = convertView.findViewById(R.id.ivEditar);
            ImageView ivEliminar = convertView.findViewById(R.id.ivEliminar);

            if (actividad != null) {
                tvActividadNombre.setText(actividad.Nombre + " (Estado: " + actividad.Estado + ")");

                ivEditar.setOnClickListener(v -> {
                    Intent intent = new Intent(ActividadesActivity.this, AddActividadActivity.class);
                    intent.putExtra("proyecto_id", proyectoId);
                    intent.putExtra("actividad_id", actividad.Id);
                    actividadLauncher.launch(intent);
                });

                ivEliminar.setOnClickListener(v -> {
                    new AlertDialog.Builder(ActividadesActivity.this)
                            .setTitle("Confirmar eliminación")
                            .setMessage("¿Está seguro de eliminar la actividad '" + actividad.Nombre + "'?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                SqlAdmin tempDb = new SqlAdmin(ActividadesActivity.this);
                                Actividades tempActividades = new Actividades(tempDb);
                                Proyectos tempProyectos = new Proyectos(tempDb);
                                try {
                                    if (tempActividades.Delete(actividad.Id)) {
                                        tempActividades.recalcularPorcentajeAvance(tempProyectos, proyectoId);
                                        cargarActividades();
                                        setResult(RESULT_OK);
                                        Toast.makeText(ActividadesActivity.this, "Actividad eliminada", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ActividadesActivity.this, "Error al eliminar actividad", Toast.LENGTH_SHORT).show();
                                    }
                                } finally {
                                    tempDb.close();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                });
            }

            return convertView;
        }
    }
}