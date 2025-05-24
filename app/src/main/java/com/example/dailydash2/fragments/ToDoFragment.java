package com.example.dailydash2.fragments;

import android.content.Context;
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

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailydash2.R;
import com.example.dailydash2.adapters.ToDoAdapter;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_to_do, container, false);

        rememberToken = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("remember_token", null);

        recyclerView = view.findViewById(R.id.todoRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ToDoAdapter(requireContext(), todoList, rememberToken);
        recyclerView.setAdapter(adapter);

        Button createBtn = view.findViewById(R.id.createTodoButton);
        createBtn.setOnClickListener(v -> {
            Fragment formFragment = new ToDoFormFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, formFragment)
                    .addToBackStack(null)
                    .commit();
        });

        loadTodos();
        return view;
    }

    private void loadTodos() {
        String url = "http://192.168.0.102/ProyectoDAM/get_todos.php";

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
}
