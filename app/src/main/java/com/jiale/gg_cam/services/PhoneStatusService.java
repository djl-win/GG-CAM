package com.jiale.gg_cam.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * phoneStatusService
 * <p>
 * phone Status Service logic when phone status changed to register boardcast
 * </p>
 *
 * @author Jiale Dong
 * @version 1.0
 * @since 2023-09-13
 */
public class PhoneStatusService extends Service {

    // Flag to track if the device is connected to WiFi
    private boolean isConnectedToWifi = false;
    // Flag to track if the device is connected to cellar
    private boolean isConnectedToCellar = false;

    // network manager
    private ConnectivityManager cm;

    // network call back
    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                isConnectedToWifi = true;
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                isConnectedToCellar = true;
            }
            decideUploadAction();
        }

        @Override
        public void onLost(@NonNull Network network) {
            NetworkCapabilities lostNetworkCapabilities = cm.getNetworkCapabilities(network);
            if (lostNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                isConnectedToWifi = false;
            } else if (lostNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                isConnectedToCellar = false;
            }
            decideUploadAction();
        }
    };


    // BroadcastReceiver to handle various device status changes
    private final BroadcastReceiver statusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check if the received intent has a valid action
            if (intent.getAction() != null) {
                // Handle relevant actions
                switch (intent.getAction()) {
                    case Intent.ACTION_BATTERY_CHANGED:
                    case Intent.ACTION_POWER_CONNECTED:
                    case Intent.ACTION_POWER_DISCONNECTED:
                    case PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED:
                        // Decide the upload action based on the new status
                        decideUploadAction();
                        break;
                }
            }
        }
    };


    // Called when the service is first created
    @Override
    public void onCreate() {
        super.onCreate();

        // Registering the BroadcastReceiver to listen for relevant actions
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
        registerReceiver(statusReceiver, filter);

        // Registering the NetworkCallback to listen for network status changes
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        cm.registerNetworkCallback(builder.build(), networkCallback);

    }

    // Decide whether to start or stop the upload based on various conditions
    private void decideUploadAction() {

        // 0. 非网络状态，禁止上传
        if(!isConnectedToWifi && !isConnectedToCellar){
            stopUpload("没有网络，禁止上传");
            return;
        }


        // 1. Check if the device is in Doze mode
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null && pm.isDeviceIdleMode()) {
            stopUpload("doze状态，取消上传");
            return;
        }

        // 2. Check the device's charging status
        BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int status = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS);
        }
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        if (isCharging) {
            startUpload("充电中，开始上传");
            return;
        }

        // 3. Check the device's battery level
        int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        if (batteryLevel <= 50) {
            startUpload("未充电，电量低于50，取消上传");
            return;
        } else if (batteryLevel >= 80) {
            startUpload("未充电，电量大于80，开始上传");
            return;
        }

        // 4. Check the network status
        if (isConnectedToWifi) {
            startUpload("50-80电量，WIFI状态，开始上传");
        } else {
            startUpload("50-80电量，非WIFI状态，取消上传");
        }
    }

    // Method to start the upload process
    private void startUpload(String message) {
        // Actual upload logic would go here
        Log.d("GG-DEBUG", message);
    }

    // Method to stop the upload process
    private void stopUpload(String message) {
        // Logic to stop the upload would go here
        Log.d("GG-DEBUG", message);
    }

    // Called when the service is being destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the BroadcastReceiver
        unregisterReceiver(statusReceiver);
        // Unregister the NetworkCallback
        cm.unregisterNetworkCallback(networkCallback);
    }

    // Return null because this is an unbounded service
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}