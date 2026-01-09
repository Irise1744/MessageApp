package uk.ac.wlv.messageapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import uk.ac.wlv.messageapp.Adapter.MessageAdapter;
import uk.ac.wlv.messageapp.Database.dbHelper;
import uk.ac.wlv.messageapp.Model.Message;

public class MainActivity extends AppCompatActivity {

    Button button;
    RecyclerView recyclerView;
    MessageAdapter adapter;
    List<Message> messageList;
    dbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        recyclerView = findViewById(R.id.recyclerView);

        db = new dbHelper(this);
        messageList = new ArrayList<>();

        loadMessages();

        adapter = new MessageAdapter(this, messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        button.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, InsertActivity.class));
        });
    }

    private void loadMessages() {
        messageList.clear();

        Cursor cursor = db.getAllMessages();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(dbHelper.COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.COLUMN_TITLE));
                String content = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.COLUMN_CONTENT));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(dbHelper.COLUMN_IMAGE_PATH));

                messageList.add(new Message(id, title, content, imagePath));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMessages();
        adapter.notifyDataSetChanged();
    }
}
