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

        //Recoge el token del intent pasado
        rememberToken = requireActivity().getIntent().getStringExtra("remember_token");

        calendarView = view.findViewById(R.id.calendarView);

        //Código que se ejecuta al seleccionar una fecha en el calendario
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String fecha = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);   //Formatea la fecha para apsarla a la función siguiente
            loadToDosForSelectedDate(fecha);
        });

        return view;
    }

    //Método que solicita al servidor las tareas para X fecha
    private void loadToDosForSelectedDate(String fecha) {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("get_todos_by_date.php"), response -> {
            try {
                JSONArray array = new JSONArray(response);

                //Comprobación de que hayan tareas
                if (array.length() == 0) {
                    Toast.makeText(getContext(), "No hay tareas para ese día", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Crea un string donde cada columna correspondrá a TITULO + FECHA INICIO + → + FECHA FIN + (salto de línea)
                StringBuilder tareasTexto = new StringBuilder();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject tarea = array.getJSONObject(i);
                    String titulo = tarea.getString("titulo");
                    String inicio = tarea.getString("fechaInicio");
                    String fin = tarea.getString("fechaFin");

                    tareasTexto.append("- ").append(titulo).append(" (").append(inicio).append(" → ").append(fin).append(")\n");
                }

                //Muestra el resultado del String en una alerta
                new AlertDialog.Builder(requireContext())
                        .setTitle("Tareas para " + fecha)
                        .setMessage(tareasTexto.toString())
                        .setPositiveButton("Cerrar", null)
                        .show();

            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error al procesar datos", Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show()
        ) {
            //Parámetros para la petición POST
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
