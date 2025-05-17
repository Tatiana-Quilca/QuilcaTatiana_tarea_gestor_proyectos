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

public class MainActivity extends AppCompatActivity {
    private ListView lvProyectos;
    private FloatingActionButton fabAddProyecto;
    private ImageButton btnLogout;
    private TextView tvNoProyectos;
    private Button btnEliminarProyecto;
    private SearchView searchViewProyectos;
    private Proyectos proyectos;
    private Actividades actividades;
    private SessionManager sessionManager;
    private ProyectoAdapter proyectoAdapter;
    private List<Proyecto> listaProyectos;
    private ActivityResultLauncher<Intent> proyectoLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvProyectos = findViewById(R.id.lvProyectos);
        fabAddProyecto = findViewById(R.id.fabAddProyecto);
        btnLogout = findViewById(R.id.btnLogout);
        tvNoProyectos = findViewById(R.id.tvNoProyectos);
        btnEliminarProyecto = findViewById(R.id.btnEliminarProyecto);
        searchViewProyectos = findViewById(R.id.searchViewProyectos);

        proyectos = new Proyectos(new SqlAdmin(this));
        actividades = new Actividades(new SqlAdmin(this));
        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        listaProyectos = new ArrayList<>();
        proyectoAdapter = new ProyectoAdapter(this, listaProyectos);
        lvProyectos.setAdapter(proyectoAdapter);

        // Configurar el launcher para crear/editar proyectos
        proyectoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        cargarProyectos();
                        Toast.makeText(this, "Proyecto actualizado", Toast.LENGTH_SHORT).show();
                    }
                });

        // Clic simple: Ver actividades
        lvProyectos.setOnItemClickListener((parent, view, position, id) -> {
            Proyecto proyecto = listaProyectos.get(position);
            Intent intent = new Intent(MainActivity.this, ActividadesActivity.class);
            intent.putExtra("proyecto_id", proyecto.Id);
            startActivity(intent);
        });

        // Clic largo: Eliminar (opcional, lo dejamos por ahora)
        lvProyectos.setOnItemLongClickListener((parent, view, position, id) -> {
            Proyecto proyecto = listaProyectos.get(position);
            Intent intent = new Intent(MainActivity.this, ProyectosActivity.class);
            intent.putExtra("proyecto_id", proyecto.Id);
            proyectoLauncher.launch(intent);
            return true;
        });

        // Configurar SearchView para filtrar proyectos
        searchViewProyectos.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrarProyectos(newText);
                return true;
            }
        });

        cargarProyectos();

        fabAddProyecto.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProyectosActivity.class);
            proyectoLauncher.launch(intent);
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void cargarProyectos() {
        try {
            int usuarioId = sessionManager.getUserId();
            if (usuarioId == -1) {
                Toast.makeText(this, "Error: Sesión no válida", Toast.LENGTH_LONG).show();
                sessionManager.logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            listaProyectos = proyectos.Read_ByUsuarioId(usuarioId);
            if (listaProyectos.isEmpty()) {
                lvProyectos.setVisibility(View.GONE);
                tvNoProyectos.setVisibility(View.VISIBLE);
                fabAddProyecto.setVisibility(View.VISIBLE);
            } else {
                lvProyectos.setVisibility(View.VISIBLE);
                tvNoProyectos.setVisibility(View.GONE);
                proyectoAdapter.clear();
                proyectoAdapter.addAll(listaProyectos);
                proyectoAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar proyectos: " + e.getMessage(), Toast.LENGTH_LONG).show();
            lvProyectos.setVisibility(View.GONE);
            tvNoProyectos.setVisibility(View.VISIBLE);
            fabAddProyecto.setVisibility(View.VISIBLE);
        }
    }

    private void filtrarProyectos(String query) {
        List<Proyecto> filteredProyectos = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            filteredProyectos.addAll(proyectos.Read_ByUsuarioId(sessionManager.getUserId()));
        } else {
            for (Proyecto proyecto : listaProyectos) {
                if (proyecto.Nombre.toLowerCase().contains(query.toLowerCase())) {
                    filteredProyectos.add(proyecto);
                }
            }
        }
        listaProyectos.clear();
        listaProyectos.addAll(filteredProyectos);
        proyectoAdapter.clear();
        proyectoAdapter.addAll(filteredProyectos);
        proyectoAdapter.notifyDataSetChanged();

        if (filteredProyectos.isEmpty()) {
            lvProyectos.setVisibility(View.GONE);
            tvNoProyectos.setVisibility(View.VISIBLE);
        } else {
            lvProyectos.setVisibility(View.VISIBLE);
            tvNoProyectos.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarProyectos();
    }

    // Clase adaptador personalizado
    private class ProyectoAdapter extends ArrayAdapter<Proyecto> {
        private final LayoutInflater inflater;

        public ProyectoAdapter(MainActivity context, List<Proyecto> proyectos) {
            super(context, 0, proyectos);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_proyecto, parent, false);
            }

            Proyecto proyecto = getItem(position);
            TextView tvProyectoNombre = convertView.findViewById(R.id.tvProyectoNombre);
            ImageView ivEditar = convertView.findViewById(R.id.ivEditar);
            ImageView ivEliminar = convertView.findViewById(R.id.ivEliminar);

            if (proyecto != null) {
                tvProyectoNombre.setText(proyecto.Nombre + " (Avance: " + (int)proyecto.PorcentajeAvance + "%)");

                ivEditar.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, ProyectosActivity.class);
                    intent.putExtra("proyecto_id", proyecto.Id);
                    proyectoLauncher.launch(intent);
                });

                ivEliminar.setOnClickListener(v -> {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Confirmar eliminación")
                            .setMessage("¿Está seguro de eliminar el proyecto '" + proyecto.Nombre + "'?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                SqlAdmin tempDb = new SqlAdmin(MainActivity.this);
                                Proyectos tempProyectos = new Proyectos(tempDb);
                                try {
                                    if (tempProyectos.Delete(proyecto.Id)) {
                                        cargarProyectos();
                                        Toast.makeText(MainActivity.this, "Proyecto eliminado", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Error al eliminar proyecto", Toast.LENGTH_SHORT).show();
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