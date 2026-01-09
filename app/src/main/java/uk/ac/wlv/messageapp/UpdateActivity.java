package uk.ac.wlv.messageapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import uk.ac.wlv.messageapp.Database.dbHelper;
import uk.ac.wlv.messageapp.Helper.ImageHelper;

public class UpdateActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private ImageView ivImage;
    private Button btnSelectImage, btnUpdate;

    private dbHelper db;
    private int messageId;
    private String currentImagePath;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        ivImage = findViewById(R.id.ivImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnUpdate = findViewById(R.id.btnUpdate);

        db = new dbHelper(this);

        // Get message ID from intent
        messageId = getIntent().getIntExtra("message_id", -1);
        if (messageId == -1) {
            Toast.makeText(this, "Invalid message ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMessage();

        // Image picker launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Save image to internal storage
                        String savedPath = ImageHelper.saveImageToInternalStorage(this, uri);
                        if (savedPath != null) {
                            currentImagePath = savedPath;
                            ivImage.setImageURI(Uri.parse(savedPath));
                        }
                    }
                }
        );

        btnSelectImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnUpdate.setOnClickListener(v -> updateMessage());
    }

    private void loadMessage() {
        // Load message from DB
        var cursor = db.getMessageById(messageId);
        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.COLUMN_TITLE));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.COLUMN_CONTENT));
            String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.COLUMN_IMAGE_PATH));

            etTitle.setText(title);
            etContent.setText(content);
            currentImagePath = imagePath;

            if (imagePath != null && !imagePath.isEmpty()) {
                ivImage.setImageURI(Uri.parse(imagePath));
            }

            cursor.close();
        }
    }

    private void updateMessage() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (content.isEmpty()) {
            etContent.setError("Content cannot be empty");
            return;
        }

        int updated = db.updateMessage(messageId, title, content, currentImagePath);
        if (updated > 0) {
            Toast.makeText(this, "Message updated", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }
}
