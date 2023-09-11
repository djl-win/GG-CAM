package com.jiale.gg_cam;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.jiale.gg_cam.listeners.CustomButtonListener;
import com.jiale.gg_cam.utils.CustomAnimationUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

/**
 * Main Activity
 * <p>
 * Main activity logic
 * </p>
 *
 * @author Jiale Dong
 * @version 1.0
 * @since 2023-09-07
 */
public class HomeActivity extends AppCompatActivity {

    // Camera preview box
    private PreviewView previewViewContentCamera;

    // cameraProviderFuture
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    // Current camera direction
    private int currentCamera = CameraSelector.LENS_FACING_BACK;

    // switch camera direction
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchDirectionCamera;

    // switch camera mode
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchModeCamera;

    // open the camera component
    private MaterialCardView materialCardViewCoverCamera;

    // camera provider
    private ProcessCameraProvider cameraProvider;

    // mainExecutor
    private Executor mainExecutor;

    // status of camera
    private boolean isCameraOpened = false;

    // button to power camera
    private ImageButton imageButtonPower;

    // button of record
    private MaterialCardView materialCardViewButtonRecord;

    // button of video
    private MaterialCardView materialCardViewButtonVideo;

    // button of video normal
    private MaterialCardView materialCardViewNormalVideo;

    // button of video recording
    private MaterialCardView materialCardViewRecordingVideo;

    // recording indicator
    private LinearProgressIndicator linearProgressIndicator;

    // ImageCapture imageCapture
    private ImageCapture imageCapture;

    // VideoCapture videoCapture
    private Recording recording = null;
    private VideoCapture<Recorder> videoCapture = null;

    // location client
    private FusedLocationProviderClient fusedLocationClient;

    // camera status
    int cameraStatus = 0;

    // custom button listener
    private CustomButtonListener customButtonListener;

    // anim status
    private boolean animStatus = false;

    // image button
    private ImageButton imageButtonImages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // init views
        initViews();
        // set Listener
        setListener();
        // close camera when first open
        closeCamera();

    }

    /**
     * init view in onCreate
     */
    private void initViews() {
        previewViewContentCamera = findViewById(R.id.home_activity_preview_view_content_camera);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        switchDirectionCamera = findViewById(R.id.home_activity_switch_direction_camera);
        switchModeCamera = findViewById(R.id.home_activity_switch_mode_camera);
        materialCardViewCoverCamera = findViewById(R.id.home_activity_material_card_view_cover_camera);
        imageButtonPower = findViewById(R.id.home_activity_image_button_power);
        materialCardViewButtonRecord = findViewById(R.id.home_activity_material_card_view_button_record);
        materialCardViewButtonVideo = findViewById(R.id.home_activity_material_card_view_button_video);
        materialCardViewNormalVideo = findViewById(R.id.home_activity_material_card_view_normal_video);
        materialCardViewRecordingVideo = findViewById(R.id.home_activity_material_card_view_recording_video);
        linearProgressIndicator = findViewById(R.id.home_activity_linear_progress_indicator);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        customButtonListener = new CustomButtonListener();
        imageButtonImages = findViewById(R.id.home_activity_image_button_images);
    }

    /**
     * set Listener
     */
    private void setListener() {
        // Listener for cameraProviderFuture to see if it can be successfully initialized after the view is created
        cameraProviderFutureListener();
        // Listener for camera direction changes
        switchDirectionCameraListener();
        // Listener for camera mode changes
        switchModeCameraListener();
        // Listener for camera open and close
        imageButtonPowerListener();
        // Listener for take picture
        materialCardViewButtonRecordListener();
        // Listener for take video
        materialCardViewButtonVideoListener();
        // Listener for custom button listener
        customButtonListener();
        // Listener for images page(activity)
        imageButtonImagesListener();
    }

    /**
     * Listener for custom button listener
     */
    private void customButtonListener() {
        // loading camera status can not use any button
        customButtonListener.setListener(new CustomButtonListener.CustomEventListener() {
            @Override
            public void onEvent() {
                if(animStatus) {
                    switchDirectionCamera.setClickable(false);
                    imageButtonPower.setClickable(false);
                    materialCardViewButtonRecord.setClickable(false);
                    switchModeCamera.setClickable(false);
                    materialCardViewButtonVideo.setClickable(false);
                }else {
                    switchDirectionCamera.setClickable(true);
                    imageButtonPower.setClickable(true);
                    materialCardViewButtonRecord.setClickable(true);
                    switchModeCamera.setClickable(true);
                    materialCardViewButtonVideo.setClickable(true);
                }
            }
        });
    }


    /**
     * Listener for cameraProviderFuture to see if it can be successfully initialized after the view is created
     */
    private void cameraProviderFutureListener() {
        mainExecutor = ContextCompat.getMainExecutor(this);
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors
                Toast.makeText(this, "Some thing wrong", Toast.LENGTH_LONG).show();
            }
        }, mainExecutor);
    }

    /**
     * Listener for camera direction changes
     */
    private void switchDirectionCameraListener() {
        switchDirectionCamera.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // judge the anim status
            animStatus = true;
            customButtonListener.triggerEvent();
            // anim
            CustomAnimationUtil.imageViewAnim02(getApplication(), materialCardViewCoverCamera);
            new Handler().postDelayed(() -> {
                try {
                    switchCamera(cameraProviderFuture.get());
                    // judge the anim status
                    animStatus = false;
                    customButtonListener.triggerEvent();
                } catch (ExecutionException | InterruptedException e) {
                    Toast.makeText(this, "Some thing wrong", Toast.LENGTH_LONG).show();
                }
            }, 1000);
            new Handler().postDelayed(() -> {
                CustomAnimationUtil.imageViewAnim01(getApplication(), materialCardViewCoverCamera);
            }, 1500);


        });
    }

    /**
     * Listener for camera mode changes
     */
    private void switchModeCameraListener() {
        switchModeCamera.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // judge the anim status
            animStatus = true;
            customButtonListener.triggerEvent();

            // anim
            CustomAnimationUtil.imageViewAnim02(getApplication(), materialCardViewCoverCamera);
            new Handler().postDelayed(() -> {
                // change the camera mode
                if (cameraStatus == 0) {
                    // 当切换摄像模式后，拍照按钮不可见，摄像按钮可见
                    materialCardViewButtonRecord.setVisibility(View.GONE);
                    materialCardViewButtonVideo.setVisibility(View.VISIBLE);
                    cameraStatus = 1;
                    bindVideoPreview(cameraProvider);
                } else {
                    cameraStatus = 0;
                    // 当切换照相模式后，拍照按钮可见，摄像按钮不可见
                    materialCardViewButtonVideo.setVisibility(View.GONE);
                    materialCardViewButtonRecord.setVisibility(View.VISIBLE);
                    bindImagePreview(cameraProvider);
                }
                // judge the anim status
                animStatus = false;
                customButtonListener.triggerEvent();
            }, 1000);
            new Handler().postDelayed(() -> {
                CustomAnimationUtil.imageViewAnim01(getApplication(), materialCardViewCoverCamera);
            }, 1500);
        });

    }

    /**
     * Change camera direction
     *
     * @param cameraProvider cameraProvider
     */
    private void switchCamera(ProcessCameraProvider cameraProvider) {
        if (currentCamera == CameraSelector.LENS_FACING_BACK) {
            currentCamera = CameraSelector.LENS_FACING_FRONT;
        } else {
            currentCamera = CameraSelector.LENS_FACING_BACK;
        }
        // 如果当前为照相模式，则绑定照相preview
        if (cameraStatus == 0) {
            bindImagePreview(cameraProvider);
        }
        // 如果当前为摄影模式，则绑定摄影preview
        else {
            bindVideoPreview(cameraProvider);
        }
    }


    /**
     * 1. Create a Preview.
     * 2. Specify the desired camera LensFacing options.
     * 3. Bind the selected camera and any use case to the lifecycle.
     * 4. Connect Preview to PreviewView.
     *
     * @param cameraProvider cameraProvider
     */
    public void bindImagePreview(@NonNull ProcessCameraProvider cameraProvider) {

        // First unstrap all previous camera use cases
        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder()
                .build();

        imageCapture = new ImageCapture.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(currentCamera)
                .build();

        preview.setSurfaceProvider(previewViewContentCamera.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageCapture, preview);
    }

    /**
     * 1. Create a Preview.
     * 2. Specify the desired camera LensFacing options.
     * 3. Bind the selected camera and any use case to the lifecycle.
     * 4. Connect Preview to PreviewView.
     *
     * @param cameraProvider cameraProvider
     */
    public void bindVideoPreview(@NonNull ProcessCameraProvider cameraProvider) {

        // First unstrap all previous camera use cases
        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder()
                .build();

        Recorder recorder = new Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build();

        videoCapture = VideoCapture.withOutput(recorder);


        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(currentCamera)
                .build();

        preview.setSurfaceProvider(previewViewContentCamera.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);
    }

    /**
     * Listener for camera open and close
     */
    private void imageButtonPowerListener() {
        imageButtonPower.setOnClickListener(view -> {
            if (!isCameraOpened) {
                openCamera();
            } else {
                closeCamera();
            }

        });
    }

    /**
     * Listener for take picture
     */
    private void materialCardViewButtonRecordListener() {
        materialCardViewButtonRecord.setOnClickListener(view -> {
            saveImage();
        });
    }


    /**
     * save image
     */
    private void saveImage() {
        // check camera status
        if (!isCameraOpened) {
            return;
        }

        // judge the anim status
        animStatus = true;
        customButtonListener.triggerEvent();

        // anim
        CustomAnimationUtil.imageViewAnim02(getApplication(), materialCardViewCoverCamera);
        new Handler().postDelayed(() -> {
            // judge the anim status
            animStatus = false;
            customButtonListener.triggerEvent();
            CustomAnimationUtil.imageViewAnim01(getApplication(), materialCardViewCoverCamera);
        }, 1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // get the location and store the image
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (!addresses.isEmpty()) {

                        Address address = addresses.get(0);
                        // 创建文件名，以时间戳和地点命名
                        String cityName = address.getLocality();
                        // 详细地址
                        String detailedAddress = address.getAddressLine(0);
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        String fileName = "IMAGE_Time_" + timeStamp + "_City_" + cityName + "_Location_" + detailedAddress;

                        // 改为media存储
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/GG-CAM");

                        // 使用得到的Uri保存图片
                        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build();
                        imageCapture.takePicture(options, mainExecutor, new ImageCapture.OnImageSavedCallback() {
                            @Override
                            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                Toast.makeText(getApplicationContext(), "Photo saved", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(@NonNull ImageCaptureException exception) {
                                // 保存失败
                                Toast.makeText(getApplicationContext(), "Photo error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    // 保存失败
                    Toast.makeText(getApplicationContext(), "Photo error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * Listener for take video
     */
    private void materialCardViewButtonVideoListener() {
        materialCardViewButtonVideo.setOnClickListener(view -> {
            saveVideo();
        });
    }

    /**
     * save video
     */
    private void saveVideo() {
        // check camera status
        if (!isCameraOpened) {
            return;
        }

        if (recording != null) {
            recording.stop();
            recording = null;
            return;
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // get the location and store the image
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        // 创建文件名，以时间戳和地点命名
                        String cityName = address.getLocality();
                        // 详细地址
                        String detailedAddress = address.getAddressLine(0);
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        String fileName = "Video_Time_" + timeStamp + "_City_" + cityName + "_Location_" + detailedAddress;

                        // 在外部存储的应用专有目录下创建一个images目录
//                        File videosDir = getExternalFilesDir("videos");
//                        if (!videosDir.exists()) {
//                            videosDir.mkdirs();  // 如果目录不存在，则创建它
//                        }

                        // 在images目录下创建要保存的图片文件
//                        photoFile = new File(videosDir, fileName);

                        // 改为通过media存储
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/GG-CAM");

                        // 保存视频带有音频
                        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                                .setContentValues(contentValues).build();


                        // 根据当前的设置和配置，为即将进行的视频录制准备必要的资源。
                        recording = videoCapture.getOutput().prepareRecording(HomeActivity.this, options)
                                .withAudioEnabled() // 确保录制的视频中带有音频。
                                .start(mainExecutor, videoRecordEvent -> {
                                    if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                                        // 当视频开始录制时，此代码块将被执行。
                                        // forbid all buttons and change button style
                                        closeButtonsWhenVideoRecord();
                                        Toast.makeText(this, "Start to record", Toast.LENGTH_SHORT).show();

                                    } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                                        if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                                            // 当视频录制结束并完成保存时，此代码块将被执行。
                                            // recover all buttons and change button style
                                            openButtonsWhenVideoRecord();
                                            Toast.makeText(this, "Video saved", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // 如果在录制视频期间出现错误，我们将关闭录制，并显示一个错误消息。
                                            openButtonsWhenVideoRecord();
                                            recording.close();
                                            recording = null;
                                            String msg = "Error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                } catch (IOException e) {
                    // 保存失败
                    Toast.makeText(getApplicationContext(), "Video error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    /**
     * logic when open camera,forbid button
     */
    private void openCamera() {
        if (cameraProvider != null) {
            if (cameraStatus == 0) {
                bindImagePreview(cameraProvider);
            } else {
                bindVideoPreview(cameraProvider);
            }
        }
        // judge the anim status
        animStatus = true;
        customButtonListener.triggerEvent();

        // delay 2s to miss cover
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // judge the anim status
                animStatus = false;
                customButtonListener.triggerEvent();
                CustomAnimationUtil.imageViewAnim01(getApplication(), materialCardViewCoverCamera);
                materialCardViewCoverCamera.setVisibility(View.GONE);
            }
        }, 500);
        // set camera status open
        isCameraOpened = true;
        imageButtonPower.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_power_close));
        // forbid button
        switchDirectionCamera.setClickable(true);
        materialCardViewButtonRecord.setClickable(true);
        materialCardViewButtonVideo.setClickable(true);
        switchModeCamera.setClickable(true);
    }

    /**
     * logic when close camera,forbid button
     */
    private void closeCamera() {
        // First unstrap all previous camera use cases
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }

        // delay 2s to show cover
        CustomAnimationUtil.imageViewAnim02(getApplication(), materialCardViewCoverCamera);
        materialCardViewCoverCamera.setVisibility(View.VISIBLE);

        // set camera status open
        isCameraOpened = false;
        imageButtonPower.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_power_open));
        // recover button
        switchDirectionCamera.setClickable(false);
        materialCardViewButtonRecord.setClickable(false);
        materialCardViewButtonVideo.setClickable(false);
        switchModeCamera.setClickable(false);
    }

    /**
     * open buttons
     */
    private void openButtonsWhenVideoRecord() {
        // when stop the record, close button and change the style of recording button
        linearProgressIndicator.setVisibility(View.GONE);
        materialCardViewRecordingVideo.setVisibility(View.GONE);
        materialCardViewNormalVideo.setVisibility(View.VISIBLE);
        switchDirectionCamera.setClickable(true);
        imageButtonPower.setClickable(true);
        materialCardViewButtonRecord.setClickable(true);
        switchModeCamera.setClickable(true);
    }

    /**
     * close buttons
     */
    private void closeButtonsWhenVideoRecord() {
        // when start the record, close button and change the style of recording button
        linearProgressIndicator.setVisibility(View.VISIBLE);
        materialCardViewRecordingVideo.setVisibility(View.VISIBLE);
        materialCardViewNormalVideo.setVisibility(View.GONE);
        switchDirectionCamera.setClickable(false);
        imageButtonPower.setClickable(false);
        materialCardViewButtonRecord.setClickable(false);
        switchModeCamera.setClickable(false);
    }

    /**
     * Listener for images page(activity)
     */
    private void imageButtonImagesListener() {
        imageButtonImages.setOnClickListener(view -> {
            startActivity(new Intent(HomeActivity.this, ImagesActivity.class));
        });
    }

}