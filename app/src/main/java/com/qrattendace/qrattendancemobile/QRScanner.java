package com.qrattendace.qrattendancemobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRScanner extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.qrscanner);

        @SuppressWarnings("deprecation")
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan QR");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        @SuppressWarnings("deprecation")
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null){
            if (result.getContents() != null){
                String qrData = result.getContents();
                Toast.makeText(this, "Scanned: " + qrData, Toast.LENGTH_SHORT).show();

                // db shit here
                Intent intent = new Intent(QRScanner.this, FaceScanner.class);
                intent.putExtra("email", getIntent().getStringExtra("email"));
                intent.putExtra("qrData", qrData);
                startActivity(intent);

                finish();
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}