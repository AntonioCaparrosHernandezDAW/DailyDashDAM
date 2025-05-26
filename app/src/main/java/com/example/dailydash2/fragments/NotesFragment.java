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

    private static final String NOTES_URL = BbddConnection.getUrl("get_notes.php");
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

        if (getArguments() != null) {
            rememberToken = getArguments().getString("remember_token");
            esPremium = getArguments().getBoolean("esPremium", false);
        } else {
            rememberToken = getActivity().getIntent().getStringExtra("remember_token");
        }

        adapter = new NoteAdapter(requireContext(), noteList, rememberToken);
        recyclerView.setAdapter(adapter);

        Button createNoteButton = view.findViewById(R.id.createNoteButton);
        createNoteButton.setOnClickListener(v -> verificarCantidadNotasYCrear());

        loadNotes();

        return view;
    }

    private void loadNotes() {
        StringRequest request = new StringRequest(Request.Method.POST, NOTES_URL,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        noteList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            int idNote = obj.getInt("idNote");
                            String title = obj.getString("title");
                            String text = obj.getString("text");
                            String date = obj.getString("date");

                            noteList.add(new Note(idNote, title, text, date));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Log.e("NOTES_JSON", "Error: " + e.getMessage());
                    }
                },
                error -> Log.e("NOTES_REQUEST", "Error: " + error.getMessage())
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

    private void verificarCantidadNotasYCrear() {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("count_notes.php"),
                response -> {
                    Log.d("COUNT_NOTES_RESPONSE", response); // Añade esta línea para ver la respuesta
                    try {
                        int totalNotas = Integer.parseInt(response.trim());

                        if (esPremium) {
                            if (totalNotas >= 50) {
                                Toast.makeText(getContext(), "Has alcanzado el límite de 50 notas para usuarios Premium", Toast.LENGTH_LONG).show();
                                return;
                            }
                        } else {
                            if (totalNotas >= 15) {
                                Toast.makeText(getContext(), "Has alcanzado el límite de 15 notas para usuarios gratuitos", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

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
                },
                error -> {
                    Log.e("COUNT_NOTES_ERROR", error.toString());
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
