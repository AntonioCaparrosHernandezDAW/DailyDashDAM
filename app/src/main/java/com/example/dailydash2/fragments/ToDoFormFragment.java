package com.example.dailydash2.fragments;

import android.app.DatePickerDialog;
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

import com.example.dailydash2.R;

import java.util.Calendar;
import java.util.Locale;

public class ToDoFormFragment extends Fragment {

    private EditText titleInput, startDateInput, endDateInput;
    private Spinner prioritySpinner;
    private Button saveButton;
    private final Calendar calendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do_form, container, false);

        titleInput = view.findViewById(R.id.todoTitleInput);
        startDateInput = view.findViewById(R.id.startDateInput);
        endDateInput = view.findViewById(R.id.endDateInput);
        prioritySpinner = view.findViewById(R.id.prioritySpinner);
        saveButton = view.findViewById(R.id.saveTodoButton);

        // Cargar spinner de prioridad
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.priority_levels,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        // Configurar selección de fecha con diálogos
        startDateInput.setOnClickListener(v -> showDatePicker(startDateInput));
        endDateInput.setOnClickListener(v -> showDatePicker(endDateInput));

        // Guardar lógica (se implementará luego)
        saveButton.setOnClickListener(v -> {
            // Aquí irá el guardado en la base de datos
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
