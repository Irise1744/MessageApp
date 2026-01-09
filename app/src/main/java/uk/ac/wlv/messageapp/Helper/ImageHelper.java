package uk.ac.wlv.messageapp.Helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageHelper {

    public static String saveImageToInternalStorage(Context context, Uri imageUri) {
        try {
            // Create directory if it doesn't exist
            File directory = new File(context.getFilesDir(), "images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate unique filename
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timeStamp + ".jpg";
            File destinationFile = new File(directory, fileName);

            // Get input stream from URI
            ContentResolver contentResolver = context.getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(imageUri);

            if (inputStream == null) {
                return null;
            }

            // Copy file to internal storage
            OutputStream outputStream = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // Return the absolute path
            return destinationFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e("ImageHelper", "Error saving image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Helper method to get file path from various URI types
    public static String getFilePathFromUri(Context context, Uri uri) {
        String path = null;

        // If the URI is a file URI
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        // For content:// URIs
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor = null;
            try {
                String[] projection = {MediaStore.Images.Media.DATA};
                cursor = context.getContentResolver().query(uri, projection, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    path = cursor.getString(columnIndex);
                }
            } catch (Exception e) {
                Log.e("ImageHelper", "Error getting file path: " + e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return path;
    }
}