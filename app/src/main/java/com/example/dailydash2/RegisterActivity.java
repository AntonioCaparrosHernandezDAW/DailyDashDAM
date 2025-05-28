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
import com.example.dailydash2.models.BbddConnection;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText usernameInput, emailInput, passwordInput;
    Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.register_username);
        emailInput = findViewById(R.id.register_email);
        passwordInput = findViewById(R.id.register_password);
        registerBtn = findViewById(R.id.register_btn);
        TextView loginTextView = findViewById(R.id.loginTextView);
        Intent intent = new Intent(this, LoginActivity.class);

        //Carga la LoginActivity
        loginTextView.setOnClickListener(v -> {
            startActivity(intent);
            finish();
        });

        //Comprueba que los campos no estén vacios y registra al usuario introducido
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
        StringRequest stringRequest = new StringRequest(Request.Method.POST, BbddConnection.getUrl("register.php"), response -> {
                    String cleanResponse = response.trim();

                    //Si se recibe el texto de exito se cargará la Login Activity
                    if (cleanResponse.equalsIgnoreCase("Registro exitoso")) {
                        Intent intentLogin=new Intent(this, LoginActivity.class);
                        startActivity(intentLogin);
                        finish();
                    }
                }, error -> {
                    Toast.makeText(this, "Error de red", Toast.LENGTH_SHORT).show();
                }
        ) {
            //Envio por POST de parámetros necesarios
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
