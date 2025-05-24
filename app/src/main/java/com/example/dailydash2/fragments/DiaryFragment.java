package com.example.dailydash2.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailydash2.R;
import com.example.dailydash2.models.BbddConnection;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DiaryFragment extends Fragment {

    private EditText diaryText;
    private DatePicker datePicker;
    private String rememberToken;
    private final String GET_URL = BbddConnection.getUrl("get_diary.php");
    private final String SAVE_URL = BbddConnection.getUrl("save_diary.php");
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        diaryText = view.findViewById(R.id.diaryText);
        datePicker = view.findViewById(R.id.datePicker);
        datePicker.setMaxDate(System.currentTimeMillis());
        Button saveButton = view.findViewById(R.id.saveDiaryButton);
        progressBar = view.findViewById(R.id.diaryProgressBar);

        rememberToken = getActivity().getIntent().getStringExtra("remember_token");

        // Cargar texto al iniciar
        loadDiaryText(getSelectedDate());

        // Cargar al cambiar fecha
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {   //si no pongo el if ese raro que te lo pone el ID no funciona
            datePicker.setOnDateChangedListener((view1, year, month, day) -> {
                loadDiaryText(getSelectedDate());
            });
        }

        saveButton.setOnClickListener(v -> {
            String text = diaryText.getText().toString().trim();
            saveDiaryText(getSelectedDate(), text);
        });

        return view;
    }

    private String getSelectedDate() {
        int year = datePicker.getYear();
        int month = datePicker.getMonth() + 1;
        int day = datePicker.getDayOfMonth();
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
    }

    private void loadDiaryText(String date) {
        progressBar.setVisibility(View.VISIBLE);            // Mostrar la barra
        diaryText.setText("");                              // Limpiar texto
        diaryText.setHint("Cargando...");                   // Cambiar hint
        diaryText.setEnabled(false);                        // Bloquear escritura

        StringRequest request = new StringRequest(Request.Method.POST, GET_URL,
                response -> {
                    diaryText.setText(response.trim());      // Cargar contenido
                    diaryText.setHint("");                   // Limpiar hint (opcional)
                    diaryText.setEnabled(true);              // Habilitar escritura
                    progressBar.setVisibility(View.GONE);    // Ocultar progreso
                },
                error -> {
                    Toast.makeText(getContext(), "Error al cargar entrada", Toast.LENGTH_SHORT).show();
                    diaryText.setHint("Error al cargar");    // Mostrar mensaje útil
                    diaryText.setEnabled(true);              // Permitir intentar escribir
                    progressBar.setVisibility(View.GONE);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("token", rememberToken);
                map.put("date", date);
                return map;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }




    private void saveDiaryText(String date, String text) {
        StringRequest request = new StringRequest(Request.Method.POST, SAVE_URL,
                response -> {
                    if (response.trim().equals("OK")) {
                        Toast.makeText(getContext(), "Guardado correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Error al guardar: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("token", rememberToken);
                map.put("date", date);
                map.put("text", text);
                return map;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }
}

