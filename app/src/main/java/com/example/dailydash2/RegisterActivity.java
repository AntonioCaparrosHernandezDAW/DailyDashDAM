package com.example.dailydash2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText usernameInput, emailInput, passwordInput;
    Button registerBtn;

    private static final String REGISTER_URL = "http://192.168.0.102/ProyectoDAM/register.php"; // Cambia a tu IP local si es necesario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.register_username);
        emailInput = findViewById(R.id.register_email);
        passwordInput = findViewById(R.id.register_password);
        registerBtn = findViewById(R.id.register_btn);

        TextView registerTextView = findViewById(R.id.loginTextView);

        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        registerBtn.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(username, email, password);
            }
        });
    }

    private void registerUser(String username, String email, String password) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                response -> {
                    Log.d("REGISTER_RESPONSE", "Respuesta del servidor: " + response.trim());
                    switch (response.trim()) {
                        case "REGISTRO_OK":
                            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                            // Ir al login o MainPage si quieres
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                            break;
                        case "ERROR1":
                            Toast.makeText(this, "Faltan campos", Toast.LENGTH_SHORT).show();
                            break;
                        case "ERROR2":
                            Toast.makeText(this, "El usuario o email ya existe", Toast.LENGTH_SHORT).show();
                            break;
                        case "ERROR3":
                            Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(this, "Error desconocido: " + response, Toast.LENGTH_SHORT).show();
                            break;
                    }
                },
                error -> {
                    Log.e("REGISTER_ERROR", "Error: " + error.getMessage());
                    Toast.makeText(this, "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
