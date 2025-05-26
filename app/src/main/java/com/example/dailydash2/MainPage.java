package com.example.dailydash2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    List<String> menuItems = Arrays.asList(
            "Notas", "Diario", "Calendario", "Tareas", "Comprar Premium"
    );

    ArrayAdapter<String> adapter;
    String rememberToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        rememberToken = getIntent().getStringExtra("remember_token");
        Log.d("TOKEN_MAINPAGE", "Token recibido: " + rememberToken);

        drawerLayout = findViewById(R.id.drawer_layout);
        menuList = findViewById(R.id.menu_list);
        menuIcon = findViewById(R.id.menu_icon);
        userMenuIcon = findViewById(R.id.user_menu_icon);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuItems);
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
                case 4:
                    verificarYMostrarPremiumDialog();
                    return;
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
                int itemId = item.getItemId();
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
                } else if (itemId == R.id.menu_logout) {
                    Intent intent = new Intent(MainPage.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // Cargar fragmento inicial
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new NotesFragment())
                .commit();
    }

    private void verificarYMostrarPremiumDialog() {
        StringRequest request = new StringRequest(Request.Method.POST,
                BbddConnection.getUrl("check_premium.php"),
                response -> {
                    boolean esPremium = response.trim().equals("1");

                    if (esPremium) {
                        Toast.makeText(this, "Ya eres Premium", Toast.LENGTH_SHORT).show();
                    } else {
                        new PremiumDialogFragment().show(getSupportFragmentManager(), "PremiumDialog");
                    }
                },
                error -> {
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
