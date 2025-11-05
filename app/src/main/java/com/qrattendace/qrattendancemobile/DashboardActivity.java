package com.qrattendace.qrattendancemobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        userEmail = getIntent().getStringExtra("email");


        Button viewAttendanceBtn = findViewById(R.id.ViewAttendanceBtn);
        viewAttendanceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ViewAttendanceActivity.class);
            intent.putExtra("email", userEmail);
            startActivity(intent);
        });

        Button QRButton = findViewById(R.id.QRBtn);
        QRButton.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, QRScanner.class);

            intent.putExtra("email", userEmail);
            startActivity(intent);
        });
    }
}
