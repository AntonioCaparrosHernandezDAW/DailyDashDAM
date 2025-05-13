package com.example.dailydash2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailydash2.R;

import java.util.HashMap;
import java.util.Map;

public class FormNoteFragment extends Fragment {

    private static final String CREATE_URL = "http://192.168.0.102/ProyectoDAM/create_note.php";
    private static final String UPDATE_URL = "http://192.168.0.102/ProyectoDAM/update_note.php";

    private EditText titleInput, textInput;
    private int noteId = -1; // -1 indica creación

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_form_note, container, false);

        titleInput = view.findViewById(R.id.noteTitleInput);
        textInput = view.findViewById(R.id.noteTextInput);
        Button saveButton = view.findViewById(R.id.saveNoteButton);

        // Recuperar token
        String token = getActivity().getIntent().getStringExtra("remember_token");

        // Recuperar datos si estamos editando
        Bundle args = getArguments();
        if (args != null) {
            noteId = args.getInt("idNote", -1);
            titleInput.setText(args.getString("title", ""));
            textInput.setText(args.getString("text", ""));
        }

        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String text = textInput.getText().toString().trim();

            if (title.isEmpty() || text.isEmpty()) {
                Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = (noteId == -1) ? CREATE_URL : UPDATE_URL;

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        if (response.trim().equals("OK")) {
                            Toast.makeText(getContext(), "Nota guardada", Toast.LENGTH_SHORT).show();
                            requireActivity().getSupportFragmentManager().popBackStack(); // Vuelve atrás
                        } else {
                            Toast.makeText(getContext(), "Error al guardar: " + response, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(getContext(), "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("token", token);
                    params.put("title", title);
                    params.put("text", text);
                    if (noteId != -1) {
                        params.put("idNote", String.valueOf(noteId));
                    }
                    return params;
                }
            };

            Volley.newRequestQueue(requireContext()).add(request);
        });

        return view;
    }
}
