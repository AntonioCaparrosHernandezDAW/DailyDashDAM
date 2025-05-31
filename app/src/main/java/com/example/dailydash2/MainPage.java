package com.example.dailydash2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailydash2.fragments.CalendarFragment;
import com.example.dailydash2.fragments.DiaryFragment;
import com.example.dailydash2.fragments.NotesFragment;
import com.example.dailydash2.fragments.PremiumDialogFragment;
import com.example.dailydash2.fragments.ProfileFragment;
import com.example.dailydash2.fragments.ToDoFragment;
import com.example.dailydash2.models.BbddConnection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPage extends AppCompatActivity {
    DrawerLayout drawerLayout;
    ListView menuList;
    ImageView menuIcon, userMenuIcon;
    ArrayAdapter<String> adapter;
    String rememberToken;
    boolean esPremium = false;

    //Lista de opciones del menú lateral
    List<String> menuItems = Arrays.asList("Notas", "Diario", "Calendario", "Tareas", "Comprar Premium");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        //Recoge el Token
        rememberToken = getIntent().getStringExtra("remember_token");

        drawerLayout = findViewById(R.id.drawer_layout);
        menuList = findViewById(R.id.menu_list);
        menuIcon = findViewById(R.id.menu_icon);
        userMenuIcon = findViewById(R.id.user_menu_icon);

        //Adaptador para el menú lateral
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuItems);
        menuList.setAdapter(adapter);

        //Abre el menú lateral
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        //Acción que se ejecuta al pulsar en cualquier acción del menú lateral
        menuList.setOnItemClickListener((parent, view, position, id) -> {
            Fragment selectedFragment = null;

            //Crea un paquete con los parámetros que se pasarán
            Bundle args = new Bundle();
            args.putString("remember_token", rememberToken);
            args.putBoolean("esPremium", esPremium);

            //Selecciona un fragmento según la opción que haya seleccionado el usuario
            switch (position) {
                case 0:
                    selectedFragment = new NotesFragment();
                    break;
                case 1:
                    selectedFragment = new DiaryFragment();
                    break;
                case 2:
                    selectedFragment = new CalendarFragment();
                    break;
                case 3:
                    selectedFragment = new ToDoFragment();
                    break;
                case 4:
                    //Comprueba si mostrar el dialog sobre el premium
                    checkAndShowPremiumDialog();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return;
            }

            //Carga el fragmento
            if (selectedFragment != null) {
                selectedFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            //Cierra el menú lateral
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        //Gestór de acción al pulsar en el icono de usuario (arriba derecha)
        userMenuIcon.setOnClickListener(v -> {
            //Se crea un menú emergente
            PopupMenu popup = new PopupMenu(MainPage.this, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.user_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                //Si se selecciona la primera opción se carga el fragmento del perfil
                if (itemId == R.id.menu_profile) {
                    ProfileFragment profileFragment = new ProfileFragment();
                    Bundle args = new Bundle();
                    args.putString("remember_token", rememberToken);
                    profileFragment.setArguments(args);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, profileFragment)
                            .addToBackStack(null)
                            .commit();
                    return true;

                //Si se selecciona la otra opción se carga la LoginActivity
                } else if (itemId == R.id.menu_logout) {
                    Intent intent = new Intent(MainPage.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);   //Inicia una nueva task y borra el historial de modo que el usuario no pueda pulsar "volver atrás" y retroceder a la MainPage
                    startActivity(intent);
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // Cargar fragmento de notas (es el fragmento por defecto)
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotesFragment())
                .commit();

        checkPremium(rememberToken);
    }

    //Comprueba en la base de datos si el usuario es premium
    private void checkPremium(String token) {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("check_premium.php"), response -> {
                    esPremium = response.trim().equals("1");
                    choosePremiumMenuText(esPremium);
                }, error -> {
                    esPremium = false;
                    choosePremiumMenuText(false);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("token", rememberToken);
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    //Cambia el texto de la 5º opción del menú lateral en función de si el usuario es premium o no
    private void choosePremiumMenuText(boolean premium) {
        menuItems.set(4, premium ? "Ya eres Premium" : "Comprar Premium");
        adapter.notifyDataSetChanged(); //Avisa de que hay que volver a cargar el contenido
    }

    //Comprueba si el usuario es premium y si ya lo es no abre el Dialog de premium
    private void checkAndShowPremiumDialog() {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("check_premium.php"), response -> {
                    boolean esPremium = response.trim().equals("1");

                    if (esPremium) {
                        Toast.makeText(this, "Ya eres Premium", Toast.LENGTH_SHORT).show();
                    } else {
                        new PremiumDialogFragment().show(getSupportFragmentManager(), "PremiumDialog");
                    }
                },  error -> {
                    Toast.makeText(this, "Error al verificar estado premium", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("token", rememberToken);
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
