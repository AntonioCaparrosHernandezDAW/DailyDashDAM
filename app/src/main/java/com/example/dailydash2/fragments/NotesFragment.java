package com.example.dailydash2.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailydash2.R;
import com.example.dailydash2.adapters.NoteAdapter;
import com.example.dailydash2.models.BbddConnection;
import com.example.dailydash2.models.Note;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotesFragment extends Fragment {
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<Note> noteList = new ArrayList<>();
    private String rememberToken;
    private boolean esPremium = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        recyclerView = view.findViewById(R.id.notesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Recupera el token y el valor premium
        if (getArguments() != null) {
            rememberToken = getArguments().getString("remember_token");
            esPremium = getArguments().getBoolean("esPremium", false);
        } else {
            rememberToken = getActivity().getIntent().getStringExtra("remember_token");
        }

        //Configuración del adaptador de notas
        adapter = new NoteAdapter(requireContext(), noteList, rememberToken);
        recyclerView.setAdapter(adapter);

        //Lógica del botón de crear nota
        Button createNoteButton = view.findViewById(R.id.createNoteButton);
        createNoteButton.setOnClickListener(v -> checkNotesAndCreate());

        //Carga las notas del usuario
        loadNotes();

        return view;
    }

    private void loadNotes() {
        StringRequest request = new StringRequest(Request.Method.POST, BbddConnection.getUrl("get_notes.php"), response -> {
            try {
                JSONArray jsonArray = new JSONArray(response);
                noteList.clear();

                //Crea un listado de notas con los datos recuperados
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    int idNote = obj.getInt("idNote");
                    String title = obj.getString("title");
                    String text = obj.getString("text");
                    String date = obj.getString("date");

                    noteList.add(new Note(idNote, title, text, date));
                }

                adapter.notifyDataSetChanged(); //Actualiza la lista de notas
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Error al procesar las notas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(getContext(), "Error de red al cargar notas", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", rememberToken);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    //Comprueba la cantidad de notas pre existentes y si no cumple con el límite accede al formulario de creación
    private void checkNotesAndCreate() {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("count_notes.php"), response -> {
            try {
                int totalNotas = Integer.parseInt(response.trim());

                //Comprueba si es premium y en base al resultado comprueba su máximo
                if (esPremium) {
                    if (totalNotas >= 50) {
                        Toast.makeText(getContext(), "Has alcanzado el límite de 50 notas para usuarios Premium", Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    if (totalNotas >= 10) {
                        Toast.makeText(getContext(), "Has alcanzado el límite de 10 notas para usuarios gratuitos", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                //Prepara el paquete de datos y carga el formulario de creación de nota
                Fragment createNoteFragment = new FormNoteFragment();
                Bundle args = new Bundle();
                args.putString("remember_token", rememberToken);
                args.putBoolean("esPremium", esPremium);
                createNoteFragment.setArguments(args);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, createNoteFragment)
                        .addToBackStack(null)
                        .commit();

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Error procesando datos", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", rememberToken);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

}
