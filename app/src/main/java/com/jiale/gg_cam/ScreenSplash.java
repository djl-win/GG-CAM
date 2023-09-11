package com.jiale.gg_cam;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.provider.Settings;

/**
 * ScreenSplash Activity
 * <p>
 * Show screen splash
 * </p>
 * @author Jiale Dong
 * @version 1.0
 * @since 2023-09-07
 */
public class ScreenSplash extends AppCompatActivity {

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int REQUEST_CODE_PERMISSIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Show splash for 2 seconds then request permissions
        new Handler().postDelayed(this::requestPermissions, 2000);
    }

    /**
     * Check and request for camera and audio permissions
     */
    private void requestPermissions() {
        if (allPermissionsGranted()) {
            startActivity(new Intent(ScreenSplash.this, HomeActivity.class));
            return;
        }

        if (shouldShowRequestPermissionRationale()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        } else {
            showPermissionExplanationDialog();
        }
    }

    /**
     * Check if all permissions are granted
     * @return true if all permissions are granted, false otherwise
     */
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if should show request permission rationale for any permission
     * @return true if rationale should be shown for any permission, false otherwise
     */
    private boolean shouldShowRequestPermissionRationale() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Show a dialog explaining why permissions are necessary
     */
    private void showPermissionExplanationDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Camera, audio and location permissions are necessary so that we can provide core functionality. Please go to Settings to enable.")
                .setPositiveButton("Go to setting", (dialog, which) -> openAppSystemSettings())
                .setNegativeButton("cancel", (dialogInterface, i) -> finish())
                .show();
    }

    /**
     * Open the system settings for this app
     */
    private void openAppSystemSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * Handle the result of permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startActivity(new Intent(ScreenSplash.this, HomeActivity.class));
            } else {
                finish();
            }
        }
    }
}
