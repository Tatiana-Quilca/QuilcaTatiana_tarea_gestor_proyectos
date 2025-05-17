package ec.edu.utn.com.example.quilcatatiana_tarea_proyectos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RecoverPasswordActivity extends AppCompatActivity {
    private EditText txtEmail;
    private Button btnRecoverPassword, btnVolver;
    private Usuarios usuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        txtEmail = findViewById(R.id.txtEmail);
        btnRecoverPassword = findViewById(R.id.btnRecoverPassword);
        btnVolver = findViewById(R.id.btnVolver);

        usuarios = new Usuarios(new SqlAdmin(this));

        btnRecoverPassword.setOnClickListener(v -> {
            String email = txtEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor, ingrese su correo electrónico", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar si el usuario existe usando la clase Usuarios
            Usuario usuario = usuarios.RecoverPassword(email);
            if (usuario != null) {
                Toast.makeText(this, "Tu contraseña actual es: " + usuario.Password, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Correo electrónico no encontrado", Toast.LENGTH_SHORT).show();
            }
        });

        btnVolver.setOnClickListener(v -> {
            startActivity(new Intent(RecoverPasswordActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarios != null && usuarios.sqlDb != null) {
            usuarios.sqlDb.close();
        }
    }
}