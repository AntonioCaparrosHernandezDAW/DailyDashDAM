package com.example.dailydash2.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailydash2.R;
import com.example.dailydash2.models.BbddConnection;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private EditText usernameInput, newPasswordInput;
    private Button updateUsernameButton, updatePasswordButton;
    private String rememberToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        usernameInput = view.findViewById(R.id.usernameInput);
        newPasswordInput = view.findViewById(R.id.newPasswordInput);
        updateUsernameButton = view.findViewById(R.id.updateUsernameButton);
        updatePasswordButton = view.findViewById(R.id.updatePasswordButton);

        //Recoge el valor de remember_token si los argumentos no son null
        rememberToken = getArguments() != null ? getArguments().getString("remember_token") : null;

        //Carga el nombre del usuario
        loadCurrentUsername();

        //Lógica al pulsar el botón de cambiar nombre de usuario, comprueba que el campo no esté vacío
        updateUsernameButton.setOnClickListener(v -> {
            String newUsername = usernameInput.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(getContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            } else {
                updateUsername(newUsername);
            }
        });

        //Misma lógica que el cambio de nombre de usuario pero con la contraseña
        updatePasswordButton.setOnClickListener(v -> {
            String newPassword = newPasswordInput.getText().toString().trim();
            if (newPassword.isEmpty()) {
                Toast.makeText(getContext(), "Introduce una contraseña nueva", Toast.LENGTH_SHORT).show();
            } else {
                updatePassword(newPassword);
            }
        });

        return view;
    }

    //Carga el username actual
    private void loadCurrentUsername() {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("get_username.php"), response -> {
            String cleanResponse = response.trim();

            if (!cleanResponse.toLowerCase().startsWith("error")) {
                usernameInput.setText(cleanResponse);   //Correcto
            } else {
                Toast.makeText(getContext(), "ERROR: " + cleanResponse, Toast.LENGTH_SHORT).show(); //Error
            }
        }, error -> Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("token", rememberToken);
                return map;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    //Lógica para cambiar el username en la base de datos
    private void updateUsername(String newUsername) {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("update_username.php"), response -> {
            if (response.trim().equals("OK")) {
                Toast.makeText(getContext(), "Nombre actualizado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error: " + response, Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("token", rememberToken);
                map.put("username", newUsername);
                return map;
            }
        };
        Volley.newRequestQueue(requireContext()).add(request);
    }

    //Lógica para cambiar la contraseña en la base de datos
    private void updatePassword(String newPassword) {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("update_password.php"), response -> {
            if (response.trim().equals("OK")) {
                Toast.makeText(getContext(), "Contraseña modificada", Toast.LENGTH_SHORT).show();
                newPasswordInput.setText("");
            } else {
                Toast.makeText(getContext(), "Error: " + response, Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("token", rememberToken);
                map.put("password", newPassword);
                return map;
            }
        };
        Volley.newRequestQueue(requireContext()).add(request);
    }
}
