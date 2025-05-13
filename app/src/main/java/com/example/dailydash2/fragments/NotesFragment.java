package com.example.dailydash2.fragments;

import android.graphics.Color;
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

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailydash2.R;
import com.example.dailydash2.adapters.NoteAdapter;
import com.example.dailydash2.models.Note;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotesFragment extends Fragment {

    private static final String NOTES_URL = "http://192.168.0.102/ProyectoDAM/get_notes.php";
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private List<Note> noteList = new ArrayList<>();
    private String rememberToken;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        recyclerView = view.findViewById(R.id.notesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        rememberToken = getActivity().getIntent().getStringExtra("remember_token"); // 👈 ¡mueve esto aquí!
        adapter = new NoteAdapter(requireContext(), noteList, rememberToken);
        recyclerView.setAdapter(adapter);

        Button createNoteButton = view.findViewById(R.id.createNoteButton);
        createNoteButton.setOnClickListener(v -> {
            Fragment createNoteFragment = new FormNoteFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, createNoteFragment)
                    .addToBackStack(null)
                    .commit();
        });

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
}