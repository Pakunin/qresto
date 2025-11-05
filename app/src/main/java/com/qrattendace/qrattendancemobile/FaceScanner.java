package com.qrattendace.qrattendancemobile;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FaceScanner extends AppCompatActivity {

    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private FaceDetector detector;
    private final OkHttpClient client = new OkHttpClient();

    private String studentEmail;
    private String qrData;
    private boolean hasMarkedAttendance = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.facescanner);

        studentEmail = getIntent().getStringExtra("email");
        qrData = getIntent().getStringExtra("qrData");
        TextView qrText = findViewById(R.id.txtQRData);
        qrText.setText(String.format(qrData));

        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .enableTracking()
                .build();

        detector = FaceDetection.getClient(options);
        startCamera();
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    @SuppressLint("UnsafeOptInUsageError")
                    ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();

                    if (imageProxy.getImage() != null) {
                        InputImage inputImage = InputImage.fromMediaImage(
                                imageProxy.getImage(),
                                imageProxy.getImageInfo().getRotationDegrees()
                        );

                        boolean[] faceDetected = {false};

                        detector.process(inputImage)
                                .addOnSuccessListener(faces -> {
                                    if (!faces.isEmpty() && !hasMarkedAttendance) {
                                        hasMarkedAttendance = true; // prevent multiple triggers
                                        runOnUiThread(() -> {
                                            Toast.makeText(this, "Face detected!", Toast.LENGTH_SHORT).show();

                                            new android.os.Handler().postDelayed(() -> {
                                                markAttendance();
                                                Toast.makeText(this, "Attendance marked", Toast.LENGTH_SHORT).show();
                                                cameraProvider.unbindAll();
                                                finish();
                                            }, 5000);
                                        });
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("FaceScanner", "Detection failed: " + e))
                                .addOnCompleteListener(task -> imageProxy.close());
                    } else {
                        imageProxy.close();
                    }
                });

                CameraSelector cameraSelector =
                        new CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                                .build();

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("FaceScanner", "Camera init failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // backend call
    private void markAttendance() {
        new Thread(() -> {
            try {
                String[] parts = qrData.split(":");
                String courseName = parts.length > 0 ? parts[0].trim() : "Unknown";
                String courseCode = parts.length > 1 ? parts[1].trim() : "N/A";


                String json = "{ \"studentEmail\":\"" + studentEmail + "\", " +
                        "\"courseName\":\"" + courseName + "\", " +
                        "\"courseCode\":\"" + courseCode + "\" }";

                String result = ApiClient.markAttendance(json);

                runOnUiThread(() ->
                        Toast.makeText(this, result, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
