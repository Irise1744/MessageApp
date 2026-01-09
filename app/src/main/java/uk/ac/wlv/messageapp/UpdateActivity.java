package uk.ac.wlv.messageapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;

import uk.ac.wlv.messageapp.Database.dbHelper;
import uk.ac.wlv.messageapp.Helper.ImageHelper;

public class UpdateActivity extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 100;
    private static final int CAMERA_PERMISSION_CODE = 200;

    EditText etTitle, etContent;
    ImageView imagePreview;
    Button btnAddImage, btnUpdate;

    String imagePath = null;
    int messageId;

    ImageCapture imageCapture;
    private dbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        imagePreview = findViewById(R.id.imagePreview);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnUpdate = findViewById(R.id.btnUpdate);

        dbHelper = new dbHelper(this);

        // Get message ID from intent
        messageId = getIntent().getIntExtra("message_id", -1);
        if (messageId == -1) {
            Toast.makeText(this, "Invalid message ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMessageData();

        btnAddImage.setOnClickListener(v -> showImageChooser());
        btnUpdate.setOnClickListener(v -> updateMessage());
    }

    // ==========================
    // LOAD MESSAGE FOR EDIT
    // ==========================
    private void loadMessageData() {
        Cursor cursor = dbHelper.getMessageById(messageId);
        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.COLUMN_TITLE));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.COLUMN_CONTENT));
            imagePath = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.COLUMN_IMAGE_PATH));

            etTitle.setText(title);
            etContent.setText(content);

            if (imagePath != null && !imagePath.isEmpty()) {
                imagePreview.setVisibility(View.VISIBLE);
                imagePreview.setImageURI(Uri.fromFile(new File(imagePath)));
            }

            cursor.close();
        }
    }

    // ==========================
    // IMAGE CHOOSER
    // ==========================
    private void showImageChooser() {
        String[] options = {"Choose from Gallery", "Take Photo"};

        new AlertDialog.Builder(this)
                .setTitle("Add Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openGallery();
                    } else {
                        openCamera();
                    }
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_camera, null);
        PreviewView previewView = view.findViewById(R.id.previewView);
        Button btnCapture = view.findViewById(R.id.btnCapture);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        startCamera(previewView);

        btnCapture.setOnClickListener(v -> takePhoto(dialog));

        dialog.show();
    }

    private void startCamera(PreviewView previewView) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto(AlertDialog dialog) {
        if (imageCapture == null) return;

        File photoFile = new File(
                getExternalFilesDir(null),
                "IMG_" + System.currentTimeMillis() + ".jpg"
        );

        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                options,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        imagePath = photoFile.getAbsolutePath();

                        imagePreview.setVisibility(View.VISIBLE);
                        imagePreview.setImageURI(Uri.fromFile(photoFile));

                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(UpdateActivity.this,
                                "Camera capture failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // ==========================
    // HANDLE GALLERY RESULT
    // ==========================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            // Save to internal storage
            imagePath = ImageHelper.saveImageToInternalStorage(this, imageUri);

            if (imagePath != null) {
                imagePreview.setVisibility(View.VISIBLE);
                imagePreview.setImageURI(Uri.fromFile(new File(imagePath)));
            } else {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ==========================
    // UPDATE MESSAGE
    // ==========================
    private void updateMessage() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Message content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        int result = dbHelper.updateMessage(messageId, title, content, imagePath);

        if (result != -1) {
            Toast.makeText(this, "Message updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to update message", Toast.LENGTH_SHORT).show();
        }
    }
}
