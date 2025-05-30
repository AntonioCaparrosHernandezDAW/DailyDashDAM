package com.example.dailydash2.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailydash2.R;
import com.example.dailydash2.models.BbddConnection;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ToDoFormFragment extends Fragment {
    private EditText titleInput, startDateInput, endDateInput;
    private Spinner prioritySpinner;
    private Button saveButton;
    private final Calendar calendar = Calendar.getInstance();
    private String rememberToken;
    private Integer idTarea = null;
    private boolean esPremium = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do_form, container, false);

        //Carga el color de fondo guardado
        SharedPreferences prefs = requireContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        int color = prefs.getInt("backgroundColor", Color.WHITE);
        view.setBackgroundColor(color);

        titleInput = view.findViewById(R.id.todoTitleInput);
        startDateInput = view.findViewById(R.id.startDateInput);
        endDateInput = view.findViewById(R.id.endDateInput);
        prioritySpinner = view.findViewById(R.id.prioritySpinner);
        saveButton = view.findViewById(R.id.saveTodoButton);

        //Recoge el remember_token y el premium del paquete o solo el token del intent
        if (getArguments() != null) {
            rememberToken = getArguments().getString("remember_token");
            esPremium = getArguments().getBoolean("esPremium", false);
        } else {
            rememberToken = requireActivity()
                    .getIntent()
                    .getStringExtra("remember_token");
        }

        //Selector de prioridad
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.priority_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        //Selectores de fecha de inicio y fin
        startDateInput.setOnClickListener(v -> showDatePicker(startDateInput));
        endDateInput.setOnClickListener(v -> showDatePicker(endDateInput));

        //Comprobación de si la tarea se está editando o creando en valor de si el id es >=1
        Bundle args = getArguments();
        if (args != null) {
            idTarea = args.getInt("idTarea", -1);
            if (idTarea != -1) {
                titleInput.setText(args.getString("titulo", ""));
                startDateInput.setText(args.getString("fechaInicio", ""));
                endDateInput.setText(args.getString("fechaFin", ""));

                String prioridad = args.getString("prioridad", "Media");
                int position = ((ArrayAdapter<String>) prioritySpinner.getAdapter()).getPosition(prioridad);
                prioritySpinner.setSelection(position);
            }
        }

        //Lógica del botón de guardar
        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String priority = prioritySpinner.getSelectedItem().toString();
            String start = startDateInput.getText().toString().trim();
            String end = endDateInput.getText().toString().trim();

            //Comprobación de campos vacios
            if (title.isEmpty() || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            //Formateo de fecha
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                java.util.Date startDate = sdf.parse(start);
                java.util.Date endDate = sdf.parse(end);

                //Comprobación de que la tarea no termine antes de la fecha de inicio
                if (startDate != null && endDate != null && endDate.before(startDate)) {
                    Toast.makeText(getContext(), "La fecha de fin no puede ser anterior a la de inicio", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error al comparar fechas", Toast.LENGTH_SHORT).show();
                return;
            }

            //Preparación de datos a enviar
            String url;
            Map<String, String> params = new HashMap<>();
            params.put("token", rememberToken);
            params.put("title", title);
            params.put("priority", priority);
            params.put("startDate", start);
            params.put("endDate", end);

            //Selección de acción que realidad en valor del id
            if (idTarea != null && idTarea != -1) {
                url = BbddConnection.getUrl("update_todo.php");
                params.put("idTarea", String.valueOf(idTarea));
            } else {
                url = BbddConnection.getUrl("create_todo.php");
            }

            StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
                if (response.trim().equals("OK")) {
                    Toast.makeText(getContext(), "Tarea guardada", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else {
                    Toast.makeText(getContext(), "Error: " + response, Toast.LENGTH_SHORT).show();
                }
            }, error -> Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };

            Volley.newRequestQueue(requireContext()).add(request);
        });

        return view;
    }

    //Abrir selector de fecha al pulsar sobre fecha inicio y fecha fin
    private void showDatePicker(EditText target) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            target.setText(dateStr);
        }, year, month, day).show();
    }
}
