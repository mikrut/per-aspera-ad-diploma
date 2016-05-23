package ru.mail.park.chat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.models.AttachedFile;

/**
 * Created by mikrut on 20.05.16.
 */
public class AttachmentsHelper {
    private final MessengerDBHelper dbHelper;

    public static final String LOG_TAG = AttachmentsHelper.class.getSimpleName();

    public AttachmentsHelper(Context context) {
        dbHelper = new MessengerDBHelper(context);
    }

    public static long saveAttachment(@NonNull AttachedFile file, @NonNull SQLiteDatabase db) {
        ContentValues values = file.getContentValues();
        return db.insertWithOnConflict(AttachmentsContract.AttachmentsEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public long saveAttachment(@NonNull AttachedFile file) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = saveAttachment(file, db);
        return result;
    }

    @NonNull
    private static Cursor getAttachmentsCursor(@NonNull String mid, @NonNull SQLiteDatabase db) {
        String whereCaluse = AttachmentsContract.AttachmentsEntry.COLUMN_NAME_MID + " = ?";
        String[] whereArgs = {mid};
        return db.query(AttachmentsContract.AttachmentsEntry.TABLE_NAME,
                AttachmentsContract.ATTACHMENT_PROJECTION,
                whereCaluse,
                whereArgs,
                null, // No GROUP BY
                null, // No GROUP BY filter
                null  // No ORDER BY
        );
    }

    @NonNull
    private Cursor getAttachmentsCursor(@NonNull String mid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor result = getAttachmentsCursor(mid, db);
        return result;
    }

    @NonNull
    private static List<AttachedFile> getAttachments(@NonNull Cursor cursor) {
        ArrayList<AttachedFile> attachmentsList = new ArrayList<>(cursor.getCount());

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            attachmentsList.add(new AttachedFile(cursor));
        }

        cursor.close();
        return attachmentsList;
    }

    @NonNull
    public static List<AttachedFile> getAttachments(@NonNull String mid, @NonNull SQLiteDatabase db) {
        Cursor cursor = getAttachmentsCursor(mid, db);
        return getAttachments(cursor);
    }

    @NonNull
    public List<AttachedFile> getAttachments(@NonNull String mid) {
        return getAttachments(getAttachmentsCursor(mid));
    }
}
