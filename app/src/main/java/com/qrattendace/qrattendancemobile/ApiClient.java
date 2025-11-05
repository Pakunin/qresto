package com.qrattendace.qrattendancemobile;

import android.util.Log;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    private static final String BASE_URL = "https://qr-backend-production-6749.up.railway.app";
    private static final OkHttpClient client = new OkHttpClient();

    // POST attendance to backend
    public static String markAttendance(String jsonBody) throws Exception {
        RequestBody body = RequestBody.create(
                jsonBody, MediaType.get("application/json"));
        Log.d("DEBUG_ATTEND", jsonBody);

        Request request = new Request.Builder()
                .url(BASE_URL + "/attendance/mark")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "ERROR_" + response.code();
            }
            return response.body().string();
        }
    }

    // GET attendance by student email
    public static String getAttendance(String email) throws Exception {
        Request request = new Request.Builder()
                .url(BASE_URL + "/attendance/student/" + email)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return "ERROR_" + response.code();
            }
            return response.body().string();
        }
    }
}
