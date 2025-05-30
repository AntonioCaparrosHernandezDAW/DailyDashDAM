package com.example.dailydash2.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.example.dailydash2.models.BbddConnection;

import java.util.HashMap;
import java.util.Map;

public class FormNoteFragment extends Fragment {
    private EditText titleInput, textInput;
    private int noteId = -1; //Si es -1 significa que es una nota nueva y sino significa que se estará editando una nota

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_form_note, container, false);

        //Carga el color de fondo guardado
        SharedPreferences prefs = requireContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        int color = prefs.getInt("backgroundColor", Color.WHITE);
        view.setBackgroundColor(color);

        titleInput = view.findViewById(R.id.noteTitleInput);
        textInput = view.findViewById(R.id.noteTextInput);
        Button saveButton = view.findViewById(R.id.saveNoteButton);

        //Recoge el token
        String token = getActivity().getIntent().getStringExtra("remember_token");

        //Recoger los argumentos pasados por paquete
        Bundle args = getArguments();
        if (args != null) {
            noteId = args.getInt("idNote", -1); //Recoge datos y si no encuentra le da un valor default de -1
            titleInput.setText(args.getString("title", ""));
            textInput.setText(args.getString("text", ""));
        }

        //Acción al pulsar el botón "Guardar"
        saveButton.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String text = textInput.getText().toString().trim();

            //Comprobación de campos vacios
            if (title.isEmpty() || text.isEmpty()) {
                Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            //Crear nota o editar nota en función del id de la nota
            String url = (noteId == -1) ? BbddConnection.getUrl("create_note.php") : BbddConnection.getUrl("update_note.php");

            StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
                if (response.trim().equals("OK")) {
                    Toast.makeText(getContext(), "Nota guardada", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack(); //Retrocede a el anterior fragmento
                } else {
                    Toast.makeText(getContext(), "Error al guardar: " + response, Toast.LENGTH_SHORT).show();
                }
            }, error -> Toast.makeText(getContext(), "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show()
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
