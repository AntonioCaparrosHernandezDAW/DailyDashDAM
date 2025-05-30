package com.example.dailydash2.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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

        //Cargar el backgroundColor según las preferencias existentes
        SharedPreferences prefs = requireContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        int color = prefs.getInt("backgroundColor", Color.WHITE);
        view.setBackgroundColor(color);

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

        //Asignación de funcionalidad a los botones de cambio de color
        view.findViewById(R.id.themeGray).setOnClickListener(v -> changeBackground("#EBEBEBFF", view, prefs, rememberToken));
        view.findViewById(R.id.themeBlue).setOnClickListener(v -> changeBackground("#B9DBFF", view, prefs, rememberToken));
        view.findViewById(R.id.themeOrange).setOnClickListener(v -> changeBackground("#FDD8A7FF", view, prefs, rememberToken));
        view.findViewById(R.id.themePink).setOnClickListener(v -> changeBackground("#FFBCDBFF", view, prefs, rememberToken));
        view.findViewById(R.id.themeGreen).setOnClickListener(v -> changeBackground("#BCFFC6", view, prefs, rememberToken));
        view.findViewById(R.id.themePurple).setOnClickListener(v -> changeBackground("#DFBCFF", view, prefs, rememberToken));
        view.findViewById(R.id.themeSalmon).setOnClickListener(v -> changeBackground("#FFB2B2", view, prefs, rememberToken));

        return view;
    }

    //Función para cambiar el color de fondo en todos los fragments
    private void changeBackground(String hexColor, View view, SharedPreferences prefs, String rememberToken){
        //Recoge el color y lo guarda en las preferencias compartidas
        int color = Color.parseColor(hexColor);
        prefs.edit().putInt("backgroundColor", color).apply();
        view.setBackgroundColor(color);

        //Carga de nuevo el fragmento para que se actuaalice el color de fondo
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString("remember_token", rememberToken);
        profileFragment.setArguments(args);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, profileFragment)  //no se puede cargar un new ProfileFragment() como en MainPage.java porque sino no recibiría parámetros
                .addToBackStack(null)
                .commit();
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
