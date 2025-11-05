package com.qrattendace.qrattendancemobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText email, pass;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        Button loginBtn = findViewById(R.id.loginBtn);
        email = findViewById(R.id.emailfield);
        pass = findViewById(R.id.passfield);

        loginBtn.setOnClickListener(v -> {
            String emailText = Objects.requireNonNull(email.getText()).toString().trim();
            String passText = Objects.requireNonNull(pass.getText()).toString().trim();

            if (emailText.isEmpty() || passText.isEmpty()) {
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "https://qr-backend-production-6749.up.railway.app/auth/studentLogin";

            String json = "{ \"email\":\"" + emailText + "\", \"password\":\"" + passText + "\" }";
            RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            // network call (on background thread)
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show()

                    );
                    System.out.println(e.getMessage());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String result = response.body().string();
                    Log.d("LOGIN_RESPONSE", result);

                    runOnUiThread(() -> {
                        if (result.contains("success")) {
                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                            intent.putExtra("email", emailText);
                            startActivity(intent);
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
    }
}
