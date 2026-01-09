package uk.ac.wlv.messageapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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

public class InsertActivity extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 100;
    private static final int CAMERA_PERMISSION_CODE = 200;

    EditText etTitle, etContent;
    ImageView imagePreview;
    Button btnAddImage, btnSave;

    String imagePath = null;

    ImageCapture imageCapture;
    private dbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        imagePreview = findViewById(R.id.imagePreview);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnSave = findViewById(R.id.btnSave);

        dbHelper = new dbHelper(this);

        btnAddImage.setOnClickListener(v -> showImageChooser());
        btnSave.setOnClickListener(v -> saveMessage());
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

    // ==========================
    // GALLERY
    // ==========================
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    // ==========================
    // CAMERA
    // ==========================
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
                        Toast.makeText(InsertActivity.this,
                                "Camera capture failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // ==========================
    // RESULT FROM GALLERY
    // ==========================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            imagePath = imageUri.toString();

            imagePreview.setVisibility(View.VISIBLE);
            imagePreview.setImageURI(imageUri);
        }
    }

    // ==========================
    // SAVE MESSAGE
    // ==========================
    private void saveMessage() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Message content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.insertMessage(title, content, imagePath);

        if (result != -1) {
            Toast.makeText(this, "Message saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save message", Toast.LENGTH_SHORT).show();
        }
    }
}
