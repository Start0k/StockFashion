package com.example.stockfashion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

public class menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        MaterialCardView cardIngreso   = findViewById(R.id.cardIngreso);
        MaterialCardView cardStock     = findViewById(R.id.cardStock);
        MaterialCardView cardModificar = findViewById(R.id.cardModificar);
        MaterialCardView cardDespacho  = findViewById(R.id.cardDespacho);

        cardIngreso.setOnClickListener(v -> {
                    startActivity(new Intent(this, Ingreso.class));
                }
        );

        cardStock.setOnClickListener(v -> {
                    startActivity(new Intent(this, verStock.class));
                }
        );

        cardModificar.setOnClickListener(v -> {
                    startActivity(new Intent(this, Modificar.class));
                }
        );

        cardDespacho.setOnClickListener(v -> {

                }
        );


    }
}
