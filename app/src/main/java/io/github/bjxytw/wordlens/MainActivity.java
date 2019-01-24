package io.github.bjxytw.wordlens;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import io.github.bjxytw.wordlens.camera.CameraSource;
import io.github.bjxytw.wordlens.graphic.GraphicOverlay;

public final class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUESTS = 1;
    private CameraSource cameraSource = null;
    private TextRecognitionProcessor frameProcessor;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        preview = findViewById(R.id.cameraPreview);
        graphicOverlay = findViewById(R.id.graphicOverlay);

        TextView resultText = findViewById(R.id.resultText);
        frameProcessor = new TextRecognitionProcessor(graphicOverlay, resultText);

        if (allPermissionsGranted())
            createCameraSource();
        else getRuntimePermissions();
    }

    private void createCameraSource() {
        if (cameraSource == null)
            cameraSource = new CameraSource(graphicOverlay, frameProcessor);
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (cameraSource != null)
            cameraSource.release();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (allPermissionsGranted()) {
            Log.i(TAG, "Permission granted.");
            createCameraSource();
        } else {
            Toast.makeText(this,
                    R.string.permission_request,Toast.LENGTH_LONG).show();
            Log.i(TAG, "Permission denied.");
            finish();
        }
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissionList()) {
            Log.i(TAG, "requiredPermission: " + permission);

            if (isPermissionDenied(this, permission))
                allNeededPermissions.add(permission);
        }

        if (!allNeededPermissions.isEmpty())
            ActivityCompat.requestPermissions(this,
                    allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
    }

    private String[] getRequiredPermissionList() {
        try {
            PackageInfo info = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;

            if (ps != null && ps.length > 0) return ps;
            else return new String[0];

        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissionList())
            if (isPermissionDenied(this, permission))
                return false;
        return true;
    }

    private static boolean isPermissionDenied(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED;
    }
}
