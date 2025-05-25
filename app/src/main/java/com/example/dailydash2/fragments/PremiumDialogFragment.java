package com.example.dailydash2.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dailydash2.R;
import com.example.dailydash2.models.BbddConnection;

import java.util.HashMap;
import java.util.Map;

public class PremiumDialogFragment extends DialogFragment {

    private String rememberToken;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_premium_dialog, container, false);

        Button buyButton = view.findViewById(R.id.buyButton);
        Button closeButton = view.findViewById(R.id.closeButton);

        rememberToken = requireActivity()
                .getIntent()
                .getStringExtra("remember_token");

        buyButton.setOnClickListener(v -> {
            if (rememberToken == null) {
                Toast.makeText(getContext(), "Error: token no disponible", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Activar Premium en base de datos
            StringRequest request = new StringRequest(Request.Method.POST,
                    BbddConnection.getUrl("activate_premium.php"),
                    response -> {
                        // 2. Abrir Stripe después de procesar
                        String url = "https://buy.stripe.com/test_14AdRafi0amy0NVbpTcMM00";
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);

                        dismiss();
                    },
                    error -> {
                        Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<>();
                    map.put("token", rememberToken);
                    return map;
                }
            };

            Volley.newRequestQueue(requireContext()).add(request);
        });

        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }
}
