package com.example.dailydash2.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailydash2.R;
import com.example.dailydash2.models.BbddConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private String rememberToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        rememberToken = requireActivity().getIntent().getStringExtra("remember_token");
        calendarView = view.findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String fecha = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            cargarTareasParaFecha(fecha);
        });

        return view;
    }

    private void cargarTareasParaFecha(String fecha) {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("get_todos_by_date.php"),
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);

                        if (array.length() == 0) {
                            Toast.makeText(getContext(), "No hay tareas para ese día", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        StringBuilder tareasTexto = new StringBuilder();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject tarea = array.getJSONObject(i);
                            String titulo = tarea.getString("titulo");
                            String inicio = tarea.getString("fechaInicio");
                            String fin = tarea.getString("fechaFin");

                            tareasTexto.append("- ").append(titulo)
                                    .append(" (").append(inicio).append(" → ").append(fin).append(")\n");
                        }

                        new AlertDialog.Builder(requireContext())
                                .setTitle("Tareas para " + fecha)
                                .setMessage(tareasTexto.toString())
                                .setPositiveButton("Cerrar", null)
                                .show();

                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("token", rememberToken);
                map.put("fecha", fecha);
                return map;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }
}
