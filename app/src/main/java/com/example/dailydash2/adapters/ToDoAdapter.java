package com.example.dailydash2.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
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
import com.example.dailydash2.fragments.ToDoFormFragment;
import com.example.dailydash2.models.BbddConnection;
import com.example.dailydash2.models.ToDo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ToDoViewHolder> {
    private final List<ToDo> todoList;
    private final Context context;
    private final String rememberToken;

    //Constructor
    public ToDoAdapter(Context context, List<ToDo> todoList, String rememberToken) {
        this.context = context;
        this.todoList = todoList;
        this.rememberToken = rememberToken;
    }

    //Infla el layout item_todo
    @NonNull
    @Override
    public ToDoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false);
        return new ToDoViewHolder(view);
    }

    //Asocia variables a los elementos de la nota
    static class ToDoViewHolder extends RecyclerView.ViewHolder {
        TextView title, dates;
        ImageView editIcon, deleteIcon, checkIcon;

        ToDoViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.todoTitle);
            dates = itemView.findViewById(R.id.todoDates);
            editIcon = itemView.findViewById(R.id.editIcon);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
            checkIcon = itemView.findViewById(R.id.checkIcon);
        }
    }

    //Método para inicializar los valores de cada tarea
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ToDoViewHolder holder, int position) {
        ToDo todo = todoList.get(position);
        holder.title.setText(todo.getTitulo());
        holder.dates.setText(todo.getFechaInicio() + " → " + todo.getFechaFin());

        //Cambiar color de fondo según si está completada o no
        if (todo.isCompletada()) {
            holder.itemView.setBackgroundColor(Color.parseColor("#C8E6C9")); //Verde claro
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFCDD2")); //Rojo claro
        }

        //Funcionalidad de el botón de editar
        holder.editIcon.setOnClickListener(v -> {
            //Crea un paquete con datos
            Bundle args = new Bundle();
            args.putInt("idTarea", todo.getIdTarea());
            args.putString("titulo", todo.getTitulo());
            args.putString("prioridad", todo.getPrioridad());
            args.putString("fechaInicio", todo.getFechaInicio());
            args.putString("fechaFin", todo.getFechaFin());

            ToDoFormFragment formFragment = new ToDoFormFragment();
            formFragment.setArguments(args);

            //Cambia el fragmento a ToDoFormFragment
            ((AppCompatActivity) context).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, formFragment)
                    .addToBackStack(null)
                    .commit();
        });

        //Funcionalidad de el botón de eliminar
        holder.deleteIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Seguro que quieres eliminar esta tarea?")
                    .setPositiveButton("Sí", (dialog, which) -> deleteTodo(todo.getIdTarea(), position))
                    .setNegativeButton("No", null)
                    .show();
        });

        //Funcionalidad de el botón de completar
        holder.checkIcon.setOnClickListener(v -> {
            //Operador ternario que devuelve 0 si está completada y 1 si no lo está
            int nuevoEstado = todo.isCompletada() ? 0 : 1;

            StringRequest request = new StringRequest(Request.Method.POST,
                    BbddConnection.getUrl("toggle_complete_todo.php"), response -> {
                if (response.trim().equals("OK")) {
                    todoList.get(position).setCompletada(nuevoEstado == 1);
                    notifyItemChanged(position);
                } else {
                    Toast.makeText(context, "Error al actualizar estado de la tarea", Toast.LENGTH_SHORT).show();
                }
            }, error -> Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<>();
                    map.put("token", rememberToken);
                    map.put("idTarea", String.valueOf(todo.getIdTarea()));
                    map.put("completada", String.valueOf(nuevoEstado));
                    return map;
                }
            };

            Volley.newRequestQueue(context).add(request);
        });
    }

    private void deleteTodo(int idTarea, int position) {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("delete_todo.php"), response -> {
            if (response.trim().equals("OK")) {
                todoList.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error al eliminar la tarea", Toast.LENGTH_SHORT).show();
            }
        }, error -> Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("token", rememberToken);
                map.put("idTarea", String.valueOf(idTarea));
                return map;
            }
        };

        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }
}
