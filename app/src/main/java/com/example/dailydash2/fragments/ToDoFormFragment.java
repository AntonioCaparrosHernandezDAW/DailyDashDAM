package com.example.dailydash2.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do_form, container, false);

        titleInput = view.findViewById(R.id.todoTitleInput);
        startDateInput = view.findViewById(R.id.startDateInput);
        endDateInput = view.findViewById(R.id.endDateInput);
        prioritySpinner = view.findViewById(R.id.prioritySpinner);
        saveButton = view.findViewById(R.id.saveTodoButton);

        rememberToken = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("remember_token", null);

        // Spinner de prioridad
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.priority_levels,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        // Date pickers
        startDateInput.setOnClickListener(v -> showDatePicker(startDateInput));
        endDateInput.setOnClickListener(v -> showDatePicker(endDateInput));

        // Comprobar si venimos en modo edición
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

        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String priority = prioritySpinner.getSelectedItem().toString();
            String start = startDateInput.getText().toString().trim();
            String end = endDateInput.getText().toString().trim();

            if (title.isEmpty() || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            String url;
            Map<String, String> params = new HashMap<>();
            params.put("token", rememberToken);
            params.put("title", title);
            params.put("priority", priority);
            params.put("startDate", start);
            params.put("endDate", end);

            if (idTarea != null && idTarea != -1) {
                url = BbddConnection.getUrl("update_todo.php");
                params.put("idTarea", String.valueOf(idTarea));
            } else {
                url = BbddConnection.getUrl("create_todo.php");
            }

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        if (response.trim().equals("OK")) {
                            Toast.makeText(getContext(), "Tarea guardada", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack();
                        } else {
                            Toast.makeText(getContext(), "Error: " + response, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show()
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
