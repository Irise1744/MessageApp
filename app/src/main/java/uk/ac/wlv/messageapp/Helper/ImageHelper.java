package uk.ac.wlv.messageapp.Helper;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageHelper {

    public static String saveImageToInternalStorage(Context context, Uri uri) {
        // Create a directory named 'images' inside app internal storage
        File directory = new File(context.getFilesDir(), "images");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Create a unique file name
        String fileName = "img_" + System.currentTimeMillis() + ".jpg";
        File file = new File(directory, fileName);

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(file)) {

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            // Return the absolute path (e.g., /data/user/0/.../files/images/img_123.jpg)
            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}