package com.example.dailydash2.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailydash2.R;
import com.example.dailydash2.adapters.ToDoAdapter;
import com.example.dailydash2.models.BbddConnection;
import com.example.dailydash2.models.ToDo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToDoFragment extends Fragment {

    private RecyclerView recyclerView;
    private ToDoAdapter adapter;
    private List<ToDo> todoList = new ArrayList<>();
    private String rememberToken;
    private boolean esPremium = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do, container, false);

        if (getArguments() != null) {
            rememberToken = getArguments().getString("remember_token");
            esPremium = getArguments().getBoolean("esPremium", false);
        } else {
            rememberToken = requireActivity()
                    .getIntent()
                    .getStringExtra("remember_token");
        }

        recyclerView = view.findViewById(R.id.todoRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ToDoAdapter(requireContext(), todoList, rememberToken);
        recyclerView.setAdapter(adapter);

        Button createBtn = view.findViewById(R.id.createTodoButton);
        createBtn.setOnClickListener(v -> verificarCantidadTareasYCrear());

        loadTodos();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodos(); // Recargar al volver
    }

    private void loadTodos() {
        if (rememberToken == null) return;

        String url = BbddConnection.getUrl("get_todos.php");

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        todoList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            int id = obj.getInt("idTarea");
                            String title = obj.getString("titulo");
                            String priority = obj.getString("prioridad");
                            String start = obj.getString("fechaInicio");
                            String end = obj.getString("fechaFin");
                            boolean done = obj.getInt("completada") == 1;

                            todoList.add(new ToDo(id, title, priority, start, end, done));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace()
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

    private void verificarCantidadTareasYCrear() {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("count_todos.php"),
                response -> {
                    try {
                        int totalTareas = Integer.parseInt(response.trim());

                        if (esPremium) {
                            if (totalTareas >= 50) {
                                Toast.makeText(getContext(), "Has alcanzado el límite de 50 tareas para usuarios Premium", Toast.LENGTH_LONG).show();
                                return;
                            }
                        } else {
                            if (totalTareas >= 5) {
                                Toast.makeText(getContext(), "Has alcanzado el límite de 5 tareas para usuarios gratuitos", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        // Si no ha superado el límite, abrir formulario
                        Fragment formFragment = new ToDoFormFragment();
                        Bundle args = new Bundle();
                        args.putString("remember_token", rememberToken);
                        args.putBoolean("esPremium", esPremium);
                        formFragment.setArguments(args);

                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, formFragment)
                                .addToBackStack(null)
                                .commit();

                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Error procesando datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show()
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
}
