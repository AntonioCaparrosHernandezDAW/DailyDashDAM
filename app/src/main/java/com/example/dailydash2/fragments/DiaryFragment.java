package com.example.dailydash2.fragments;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
    private boolean esPremium = false;
    private int maxLength;
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

        //Obtiene los argumentos
        if (getArguments() != null) {
            rememberToken = getArguments().getString("remember_token");
            esPremium = getArguments().getBoolean("esPremium", false);
        } else {
            rememberToken = getActivity().getIntent().getStringExtra("remember_token");
        }

        //Operador ternario para comprobar la longitud máxima de caracteres que podrá introducir el usuario
        maxLength = esPremium ? 5000 : 100;

        //Detiene al usuario de escribir el carácter pulsado si este supera el máximo de carácteres
        diaryText.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(maxLength)
        });

        //Va comprobando la cantidad de caracteres escritos para que no exceda el número máximo
        diaryText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == maxLength) {
                    diaryText.setError("Límite máximo de " + maxLength + " caracteres");
                } else {
                    diaryText.setError(null);
                }
            }

            //Funciones obligatorias de implementar pero que no se utilizarán
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //Cargar la entrada del texto al iniciar
        loadDiaryText(getSelectedDate());

        //Comprobación para comprobar la versión de android ya que solo funciona a partir de X versión
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Carga la entrada al cambiar de fecha
            datePicker.setOnDateChangedListener((view1, year, month, day) -> loadDiaryText(getSelectedDate()));
        }

        //Lógica de botón "Guardar"
        saveButton.setOnClickListener(v -> {
            String text = diaryText.getText().toString().trim();
            //Comprobación extra por si la anterior han fallado
            if (text.length() > maxLength) {
                Toast.makeText(getContext(), "No puedes escribir más de " + maxLength + " caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            saveDiaryText(getSelectedDate(), text);
        });

        return view;
    }

    //Obtiene la fecha seleccionada en un formato válido
    private String getSelectedDate() {
        int year = datePicker.getYear();
        int month = datePicker.getMonth() + 1;
        int day = datePicker.getDayOfMonth();
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
    }

    ////Carga el texto de la entrada para el día seleccionado
    private void loadDiaryText(String date) {
        progressBar.setVisibility(View.VISIBLE);
        diaryText.setText("");
        diaryText.setHint("Cargando...");
        diaryText.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.POST, BbddConnection.getUrl("get_diary.php"), response -> {
            diaryText.setText(response.trim());
            diaryText.setHint("Escriba su entrada");
            diaryText.setEnabled(true);
            progressBar.setVisibility(View.GONE);
        }, error -> {
            Toast.makeText(getContext(), "Error al cargar entrada", Toast.LENGTH_SHORT).show();
            diaryText.setHint("Error al cargar");
            diaryText.setEnabled(true);
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

    //Guarda en la base de datos la entrada introducida
    private void saveDiaryText(String date, String text) {
        StringRequest request = new StringRequest(Request.Method.POST, BbddConnection.getUrl("save_diary.php"), response -> {
            if (response.trim().equals("OK")) {
                Toast.makeText(getContext(), "Guardado correctamente", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error al guardar: " + response, Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show()
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
