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

    //Constructor
    public NoteAdapter(Context context, List<Note> notes, String token) {
        this.context = context;
        this.notes = notes;
        this.rememberToken = token;
    }

    //Infla el layout item_note
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, text, date;
        ImageView deleteIcon, editIcon;

        public NoteViewHolder(View itemView) {  //Asociar las variables a los elementos de la vista
            super(itemView);
            title = itemView.findViewById(R.id.noteTitle);
            text = itemView.findViewById(R.id.noteText);
            date = itemView.findViewById(R.id.noteDate);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
            editIcon = itemView.findViewById(R.id.editIcon);
        }
    }

    //Método para inicializar los valores de cada nota
    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.title.setText(note.title);
        holder.text.setText(note.text);
        holder.date.setText(note.date);

        //Gestión de funcionalidad de botón editar en cada nota
        holder.editIcon.setOnClickListener(v -> {
            FormNoteFragment fragment = new FormNoteFragment();

            //Se crea un paquete con parámetros a pasar
            Bundle args = new Bundle();
            args.putInt("idNote", note.idNote);
            args.putString("title", note.title);
            args.putString("text", note.text);
            fragment.setArguments(args);

            //Y se cambia el fragmento por el  FormNoteFragment
            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        //Gestión de funcionalidad de botón borrar en cada nota
        holder.deleteIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar nota")
                    .setMessage("¿Estás seguro de que quieres eliminar esta nota?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        deleteNote(note.idNote, position);
                    })     //Al confirmar la alerta se ejecuta la función deleteNote
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    //Función para borrar la nota seleccionada
    private void deleteNote(int idNote, int position) {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("delete_note.php"), response -> { //Conecta con el script php y espera respuesta
            if (response.trim().equals("OK")) { //Si es OK (eliminando espacios sobrantes con trim) elimina visualmente esa nota y el script la botta a nivel de base de datos
                notes.remove(position);
                notifyItemRemoved(position);    //Notifica a RecyclerView de que debe recargar la lista
                Toast.makeText(context, "Nota eliminada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error: " + response, Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
        ) {
            //Métodos que se pasan por POST al script
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("token", rememberToken);
                map.put("idNote", String.valueOf(idNote));
                return map;
            }
        };

        Volley.newRequestQueue(context).add(request);   //Se realiza la petición POST
    }

    //Función necesaria para que ReciclerView muestre la lista de notas
    @Override
    public int getItemCount() {
        return notes.size();
    }
}
