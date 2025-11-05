package com.qrattendace.qrattendancemobile;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class ViewAttendanceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_attendance);

        TextView attendanceText = findViewById(R.id.attendanceList);
        String email = getIntent().getStringExtra("email");

        new Thread(() -> {
            try {
                String response = ApiClient.getAttendance(email);
                if (!response.startsWith("ERROR")) {
                    JSONArray arr = new JSONArray(response);
                    StringBuilder sb = new StringBuilder();

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        sb.append("")
                                .append(obj.getString("courseName"))
                                .append(" (").append(obj.getString("courseCode")).append(")\n")
                                .append("Date: ").append(obj.getString("timestamp")).append("\n")
                                .append("Status: ").append(obj.getString("status")).append("\n\n");
                    }

                    runOnUiThread(() -> attendanceText.setText(sb.toString()));
                } else {
                    runOnUiThread(() -> attendanceText.setText("Failed to load attendance"));
                }
            } catch (Exception e) {
                Log.e("API", "Error fetching attendance", e);
            }
        }).start();
    }
}
