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
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailydash2.models.BbddConnection;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    Button loginBtn;

    private static final String LOGIN_URL = BbddConnection.getUrl("login.php");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        usernameInput = findViewById(R.id.username_input);
        passwordInput = findViewById(R.id.password_input);
        loginBtn = findViewById(R.id.login_btn);

        TextView registerTextView = findViewById(R.id.registerTextView);

        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });


        loginBtn.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(username, password);
            }
        });
    }

    private void loginUser(String username, String password) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                response -> {
                    String cleanResponse = response.trim();
                    Log.d("LOGIN_RESPONSE", "Respuesta del servidor: " + cleanResponse);

                    if (cleanResponse.startsWith("ACCESO:")) {
                        String token = cleanResponse.substring(7); // Extrae el token tras "ACCESO:"
                        Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show();

                        // Enviar el token a MainPage
                        Intent intent = new Intent(this, MainPage.class);
                        intent.putExtra("remember_token", token);
                        startActivity(intent);
                        finish();

                    } else if (cleanResponse.equals("ERROR1")) {
                        Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show();
                    } else if (cleanResponse.equals("ERROR2")) {
                        Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error desconocido: " + cleanResponse, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("LOGIN_ERROR", "Error de red: " + error.getMessage());
                    Toast.makeText(this, "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}