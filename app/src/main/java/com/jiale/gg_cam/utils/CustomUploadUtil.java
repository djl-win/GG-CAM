package com.jiale.gg_cam.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jiale.gg_cam.entities.CamItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Custom upload file classes
 * <p>
 * upload file to fire base
 * </p>
 * @author Jiale Dong
 * @version 1.0
 * @since 2023-09-16
 */
public class CustomUploadUtil {

    // firebase storage
    private static FirebaseStorage storage;
    // firebase reference of storage
    private static StorageReference storageReference;

    // status of uploading
    private static boolean statusUpload;
    // time format
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");


    // initialize varieties for firebase storage
    static {
        //  initialize storage
        storage = FirebaseStorage.getInstance();
        //  initialize reference of storage
        storageReference = storage.getReference();
        //  initialize status of uploading
        statusUpload = false;
    }

    /**
     * upload Files to firebase storage
     */
    public static void startUploadFiles(ContentResolver contentResolver, Context context){

        // 0. check the upload status of uploading
        if(statusUpload){
            Log.d("GG-DEBUG","Decline, uploading now!!!");
            return;
        }

        Toast.makeText(context, "Start uploading!", Toast.LENGTH_LONG).show();

        Log.d("GG-DEBUG","uploading!!!");

        List<CamItem> imageItems;
        List<CamItem> videoItems;

        // 1. change the upload status of uploading
        statusUpload = true;

        // 2. get all files needed to upload

        // get all image media items from the "Pictures/GG-CAM/" folder to the list
        imageItems = getAllMediaItemsFromFolder(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, CamItem.MediaType.IMAGE, contentResolver);

        // get all video media items from the "Movies/GG-CAM/" folder to the list
        videoItems = getAllMediaItemsFromFolder(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, CamItem.MediaType.VIDEO,contentResolver);

        // sort by time
        imageItems = sortItemsByTime(imageItems);
        videoItems = sortItemsByTime(videoItems);

        List<Task<StorageMetadata>> preAllTasks = new ArrayList<>();
        List<UploadTask> allTasks = new ArrayList<>();

        for (CamItem imageItem : imageItems) {
            StorageReference fileReference = storageReference.child("images/"+ imageItem.getCity() +"/" + imageItem.getFileName());
            // 检查文件是否存在
            Task<StorageMetadata> storageMetadataTask = fileReference.getMetadata().addOnSuccessListener(storageMetadata -> {
                // Metadata now contains the metadata for 'images/forest.jpg'
                // 文件已存在，不执行上传操作
                // Log.d("GG-DEBUG", "文件已存在: " + imageItem.getFileName());
            }).addOnFailureListener(exception -> {
                // 文件不存在，执行上传操作
                Log.d("GG-DEBUG", "文件不存在，开始上传: " + imageItem.getFileName());
                UploadTask uploadTask = fileReference.putFile(imageItem.getUri());
                uploadTask.addOnFailureListener(e -> {
                    Log.d("GG-DEBUG",imageItem.getFileName() +" 上传失败：" + e.getMessage());
                });
                allTasks.add(uploadTask);
            });
            preAllTasks.add(storageMetadataTask);
        }

        for (CamItem videoItem : videoItems) {
            StorageReference fileReference = storageReference.child("videos/"+ videoItem.getCity() +"/" + videoItem.getFileName());
            // 检查文件是否存在
            Task<StorageMetadata> storageMetadataTask = fileReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    // Metadata now contains the metadata for 'images/forest.jpg'
                    // 文件已存在，不执行上传操作
                    // Log.d("GG-DEBUG", "文件已存在: " + videoItem.getFileName());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // 文件不存在，执行上传操作
                    Log.d("GG-DEBUG", "文件不存在，开始上传: " + videoItem.getFileName());
                    UploadTask uploadTask = fileReference.putFile(videoItem.getUri());
                    uploadTask.addOnFailureListener(e -> {
                        Log.d("GG-DEBUG",videoItem.getFileName() +" 上传失败：" + e.getMessage());
                    });
                    allTasks.add(uploadTask);
                }
            });
            preAllTasks.add(storageMetadataTask);
        }

        // 99. If all file upload, change the status to false.
        Tasks.whenAllComplete(preAllTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {
                Tasks.whenAllComplete(allTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> task) {
                        statusUpload = false;
                        Toast.makeText(context, "Upload success",Toast.LENGTH_LONG).show();
                    }
                });
            }

        });

    }

    /**
     * stop upload Files to firebase storage
     */
    public static void stopUploadFiles(){
        statusUpload = false;
    }

    /**
     * Retrieves media items from a specific folder based on media type.
     *
     * @param uri       the content URI pointing to the media store
     * @param mediaType the type of media (image or video)
     * @return List of MediaItem objects
     */
    private static List<CamItem> getAllMediaItemsFromFolder(Uri uri, CamItem.MediaType mediaType, ContentResolver contentResolver) {

        // Create an empty list to store the media items
        List<CamItem> camItems = new ArrayList<>();

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
        if (mediaType == CamItem.MediaType.IMAGE) {
            // MIME type for JPEG images
            mimeType = "image/jpeg";
            // RELATIVE PATH for JPEG images
            relativePath = "Pictures/GG-CAM/";
        } else if (mediaType == CamItem.MediaType.VIDEO) {
            // MIME type for MP4 videos
            mimeType = "video/mp4";
            // RELATIVE PATH for MP4 videos
            relativePath = "Movies/GG-CAM/";
        } else {
            return camItems;  // If unknown mediaType, return an empty list
        }

        // Construct the selection criteria for the media store query
        String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=? " +
                "AND " +
                MediaStore.MediaColumns.MIME_TYPE + "=?";
        // Arguments for the selection criteria
        String[] selectionArgs = new String[]{relativePath, mimeType};

        // Query the media store with the given parameters
        try (Cursor cursor = contentResolver.query(
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

                // get details of file
                String[] infoFromFileName = getInfoFromFileName(fileName);

                camItems.add(new CamItem(fileName,contentUri,mediaType,infoFromFileName[0],infoFromFileName[1],infoFromFileName[2]));
            }
        }

        // Return the list of media items
        return camItems;
    }

    /**
     * Get the picture info from file name
     *
     * @param fileName file name
     * @return string[0]:city, string[1]:location, string[2]:time
     */
    private static String[] getInfoFromFileName(String fileName) {
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

    /**
     * sort list by time
     * @param camItems cam items
     * @return cam items
     */
    private static List<CamItem> sortItemsByTime(List<CamItem> camItems){
        // sort desc time
        camItems.sort(new Comparator<CamItem>() {
            @Override
            public int compare(CamItem item1, CamItem item2) {
                try {

                    long timestamp1 = Objects.requireNonNull(sdf.parse(item1.getTime())).getTime();
                    long timestamp2 = Objects.requireNonNull(sdf.parse(item2.getTime())).getTime();

                    return Long.compare(timestamp2, timestamp1);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse date string: " + e.getMessage());
                }
            }
        });
        return camItems;
    }
}
