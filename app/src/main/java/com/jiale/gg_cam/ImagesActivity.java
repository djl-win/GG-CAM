package com.jiale.gg_cam;

import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.jiale.gg_cam.adapters.ImagesListAdapter;
import com.jiale.gg_cam.entities.MediaItem;

import java.util.ArrayList;
import java.util.List;

public class ImagesActivity extends AppCompatActivity {

    // image button to change to camera page
    private ImageButton imageButtonCamera;

    // recycle view for items show
    private RecyclerView recyclerViewList;
    // 声明权限请求码，你可以自定义
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 10111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);
        // init views
        initViews();
        // set Listener
        setListener();

    }

    /**
     * init view in on Create
     */
    private void initViews() {
        imageButtonCamera = findViewById(R.id.images_activity_image_button_camera);
        recyclerViewList = findViewById(R.id.images_activity_recycle_view_list);
    }

    /**
     * set Listener
     */
    private void setListener() {
        // Listener for imageButtonCamera
        imageButtonCameraListener();

        recyclerViewList.setLayoutManager(new GridLayoutManager(this, 2)); // 2列

        ImagesListAdapter adapter = new ImagesListAdapter(this, getAllMediaFromGGCamFolders());
        recyclerViewList.setAdapter(adapter);
    }

    /**
     * Listener for imageButtonCamera
     */
    private void imageButtonCameraListener() {
        imageButtonCamera.setOnClickListener(view -> {
            startActivity(new Intent(ImagesActivity.this, HomeActivity.class));
        });
    }

    /**
     * Retrieves all media items from specific GG-CAM folders.
     */
    private List<MediaItem> getAllMediaFromGGCamFolders() {
        // Create an empty list to store the media items
        List<MediaItem> mediaItems = new ArrayList<>();

        // Add all image media items from the "Pictures/GG-CAM/" folder to the list
        mediaItems.addAll(getAllMediaItemsFromFolder(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaItem.MediaType.IMAGE));

        // Add all video media items from the "Movies/GG-CAM/" folder to the list
        mediaItems.addAll(getAllMediaItemsFromFolder(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaItem.MediaType.VIDEO));

        // Return the list containing all the media items
        return mediaItems;
    }


    /**
     * Retrieves media items from a specific folder based on media type.
     *
     * @param uri       the content URI pointing to the media store
     * @param mediaType the type of media (image or video)
     * @return List of MediaItem objects
     */
    private List<MediaItem> getAllMediaItemsFromFolder(Uri uri, MediaItem.MediaType mediaType) {

        // Create an empty list to store the media items
        List<MediaItem> mediaItems = new ArrayList<>();

        // Define the columns to be retrieved from the media store
        String[] projection = {
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.RELATIVE_PATH
        };

        // Determine the MIME type and RELATIVE PATH based on the provided mediaType enum
        String mimeType;
        String relativePath;
        if (mediaType == MediaItem.MediaType.IMAGE) {
            // MIME type for JPEG images
            mimeType = "image/jpeg";
            // RELATIVE PATH for JPEG images
            relativePath = "Pictures/GG-CAM/";
        } else if (mediaType == MediaItem.MediaType.VIDEO) {
            // MIME type for MP4 videos
            mimeType = "video/mp4";
            // RELATIVE PATH for MP4 videos
            relativePath = "Movies/GG-CAM/";
        } else {
            return mediaItems;  // If unknown mediaType, return an empty list
        }

        // Construct the selection criteria for the media store query
        String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=? " +
                "AND " +
                MediaStore.MediaColumns.MIME_TYPE + "=?";
        // Arguments for the selection criteria
        String[] selectionArgs = new String[]{relativePath, mimeType};

        // Query the media store with the given parameters
        try (Cursor cursor = getContentResolver().query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
        )) {
            // Get the column index of the media ID
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID);

            // Get the column index for DISPLAY_NAME
            int displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);

            // Iterate through the query results
            while (cursor.moveToNext()) {
                // Get the media ID
                long id = cursor.getLong(idColumn);

                // Retrieve the file name
                String fileName = cursor.getString(displayNameColumn);

                // Construct the content URI for the media item
                Uri contentUri = ContentUris.withAppendedId(uri, id);

                String[] infoFromFileName = getInfoFromFileName(fileName);
//                Log.d("GG_DEBUG", "city: " + infoFromFileName[0]);
//                Log.d("GG_DEBUG", "location:" +infoFromFileName[1]);
//                Log.d("GG_DEBUG", "time:" +infoFromFileName[2]);
                // Add the media item to the list
                mediaItems.add(new MediaItem(contentUri, mediaType,infoFromFileName[0],infoFromFileName[1],infoFromFileName[2]));
            }
        }

        // Return the list of media items
        return mediaItems;
    }


    /**
     * Get the picture info from file name
     *
     * @param fileName file name
     * @return string[0]:city, string[1]:location, string[2]:time
     */
    private String[] getInfoFromFileName(String fileName) {
        String[] parts = fileName.split("_");
        String city = "null";
        String location = "null";
        String finalDateTime = "null";

        // 1. Extract the time and convert to desired format
        if (parts[1].equals("Time")) {
            String dateStr = parts[2]; // 20230911
            String timeStr = parts[3]; // 033140

            String formattedDate = dateStr.substring(0, 4) + "/" + dateStr.substring(4, 6) + "/" + dateStr.substring(6);
            String formattedTime = timeStr.substring(0, 2) + ":" + timeStr.substring(2, 4) + ":" + timeStr.substring(4);
            finalDateTime = formattedDate + " " + formattedTime;
        }

        // 2. Extract the city
        if (parts[4].equals("City")) {
            city = parts[5]; // Mountain View
        }

        // 3. Extract the location
        if (parts[6].equals("Location")) {
            StringBuilder locationTemp = new StringBuilder(parts[7]);
            for (int i = 8; i < parts.length - 1; i++) {  // Continue until file extension
                locationTemp.append("_").append(parts[i]);
            }
            // Remove the file extension from the end of the location
            location = locationTemp.toString().replaceAll("\\.jpg$|\\.mp4$", "");
        }
        return new String[]{city,location,finalDateTime};
    }
}