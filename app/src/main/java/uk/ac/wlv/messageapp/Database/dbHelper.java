package uk.ac.wlv.messageapp.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class dbHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "blog_app.db";
    private static final int DATABASE_VERSION = 1;

    // Table Name
    public static final String TABLE_MESSAGES = "messages";

    // Columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_IMAGE_PATH = "image_path";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";
    public static final String COLUMN_UPLOADED = "uploaded";

    // Create Table SQL
    private static final String CREATE_TABLE_MESSAGES =
            "CREATE TABLE " + TABLE_MESSAGES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_CONTENT + " TEXT NOT NULL, " +
                    COLUMN_IMAGE_PATH + " TEXT, " +
                    COLUMN_CREATED_AT + " INTEGER, " +
                    COLUMN_UPDATED_AT + " INTEGER, " +
                    COLUMN_UPLOADED + " INTEGER DEFAULT 0" +
                    ");";

    public dbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    // Insert Message
    public long insertMessage(String title, String content, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_IMAGE_PATH, imagePath);
        values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
        values.put(COLUMN_UPDATED_AT, System.currentTimeMillis());
        values.put(COLUMN_UPLOADED, 0);

        return db.insert(TABLE_MESSAGES, null, values);
    }

    // Get All Messages
    public Cursor getAllMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_MESSAGES,
                null,
                null,
                null,
                null,
                null,
                COLUMN_CREATED_AT + " DESC"
        );
    }

    // Get Single Message
    public Cursor getMessageById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_MESSAGES,
                null,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );
    }

    // Update Message
    public int updateMessage(int id, String title, String content, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_IMAGE_PATH, imagePath);
        values.put(COLUMN_UPDATED_AT, System.currentTimeMillis());

        return db.update(
                TABLE_MESSAGES,
                values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    // Delete Single Message
    public int deleteMessage(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(
                TABLE_MESSAGES,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }

    // Delete Multiple Messages
    public int deleteMultipleMessages(String ids) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(
                TABLE_MESSAGES,
                COLUMN_ID + " IN (" + ids + ")",
                null
        );
    }

    // Search Messages
    public Cursor searchMessages(String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_MESSAGES,
                null,
                COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?",
                new String[]{"%" + keyword + "%", "%" + keyword + "%"},
                null,
                null,
                COLUMN_CREATED_AT + " DESC"
        );
    }

    // Mark Message as Uploaded
    public int markAsUploaded(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_UPLOADED, 1);

        return db.update(
                TABLE_MESSAGES,
                values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}
        );
    }
}
