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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_premium_dialog, container, false);

        Button buyButton = view.findViewById(R.id.buyButton);
        Button closeButton = view.findViewById(R.id.closeButton);
        rememberToken = requireActivity().getIntent().getStringExtra("remember_token");

        //Lógica al pulsar en "Comprar"
        buyButton.setOnClickListener(v -> {
            //Comprobación de token recibido
            if (rememberToken == null) {
                Toast.makeText(getContext(), "Error: token no disponible", Toast.LENGTH_SHORT).show();
                return;
            }

            /*
            * Activa premium y carga la página de compra, esto se ha hecho así dado a las limitaciones del proyecto ya que si se quiere cambiar el estado premium después de comprar en la página
            * haría falta un servidor web a donde ser redirigido y donde realizar el cambio a la base de datos. Dado que el proyecto no consta d eprogramación web dejo la página de compra igualmente
            * preparada como ejemplo de lo que vería el usuario
            *
            * Tarjeta válida para el testing de compra:
            *   Nª tarjeta: 5555 5555 5555 4444
            *   Fecha: Superior a la fecha actual
            *   Nª secreto: Cualquiera
            * */
            StringRequest request = new StringRequest(Request.Method.POST,
                    BbddConnection.getUrl("activate_premium.php"), response -> {
                String url = "https://buy.stripe.com/test_14AdRafi0amy0NVbpTcMM00";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);

                dismiss();  //Cierre de dialog
            }, error -> {
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

        //El botón de cancelar cierra el diálog
        closeButton.setOnClickListener(v -> dismiss());

        return view;
    }
}
