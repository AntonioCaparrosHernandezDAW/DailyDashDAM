package com.example.dailydash2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.dailydash2.fragments.CalendarFragment;
import com.example.dailydash2.fragments.DiaryFragment;
import com.example.dailydash2.fragments.NotesFragment;
import com.example.dailydash2.fragments.ProfileFragment;
import com.example.dailydash2.fragments.ToDoFragment;

public class MainPage extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ListView menuList;
    ImageView menuIcon, userMenuIcon;

    String[] menuItems = {"Notas", "Diario", "Calendario", "Tareas"};

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        // Obtener token desde SharedPreferences (más seguro)
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String rememberToken = prefs.getString("remember_token", null);
        Log.d("TOKEN_MAINPAGE", "Token recibido: " + rememberToken);

        drawerLayout = findViewById(R.id.drawer_layout);
        menuList = findViewById(R.id.menu_list);
        menuIcon = findViewById(R.id.menu_icon);
        userMenuIcon = findViewById(R.id.user_menu_icon);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuItems);
        menuList.setAdapter(adapter);

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        menuList.setOnItemClickListener((parent, view, position, id) -> {
            Fragment selectedFragment = null;
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
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        userMenuIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainPage.this, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.user_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.menu_profile) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .addToBackStack(null)
                            .commit();

                    return true;
                } else if (id == R.id.menu_logout) {
                    prefs.edit().remove("remember_token").apply();
                    Intent intent = new Intent(MainPage.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            popup.show();
        });

        // Cargar fragmento por defecto
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotesFragment())
                .commit();
    }
}
