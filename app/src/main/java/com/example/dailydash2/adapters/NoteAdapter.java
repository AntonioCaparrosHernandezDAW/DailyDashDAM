package com.example.dailydash2.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailydash2.R;
import com.example.dailydash2.fragments.FormNoteFragment;
import com.example.dailydash2.models.BbddConnection;
import com.example.dailydash2.models.Note;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final Context context;
    private final List<Note> notes;
    private final String rememberToken;

    public NoteAdapter(Context context, List<Note> notes, String token) {
        this.context = context;
        this.notes = notes;
        this.rememberToken = token;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, text, date;
        ImageView deleteIcon, editIcon;

        public NoteViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.noteTitle);
            text = itemView.findViewById(R.id.noteText);
            date = itemView.findViewById(R.id.noteDate);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
            editIcon = itemView.findViewById(R.id.editIcon);
        }
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.title.setText(note.title);
        holder.text.setText(note.text);
        holder.date.setText(note.date);

        holder.editIcon.setOnClickListener(v -> {
            // Lanzar fragmento de edición
            FormNoteFragment fragment = new FormNoteFragment();

            Bundle args = new Bundle();
            args.putInt("idNote", note.idNote);
            args.putString("title", note.title);
            args.putString("text", note.text);
            fragment.setArguments(args);

            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });


        holder.deleteIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar nota")
                    .setMessage("¿Estás seguro de que quieres eliminar esta nota?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        deleteNote(note.idNote, position);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void deleteNote(int idNote, int position) {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("delete_note.php"),
                response -> {
                    if (response.trim().equals("OK")) {
                        notes.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Nota eliminada", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("token", rememberToken);
                map.put("idNote", String.valueOf(idNote));
                return map;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }
}
