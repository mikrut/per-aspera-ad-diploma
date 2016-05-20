package ru.mail.park.chat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.models.AttachedFile;
import ru.mail.park.chat.models.Message;

/**
 * Created by Михаил on 26.03.2016.
 */
public class MessagesHelper {
    private final MessengerDBHelper dbHelper;
    public MessagesHelper(@NonNull Context context) {
        dbHelper = new MessengerDBHelper(context);
    }

    public long saveMessage(@NonNull Message message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = message.getContentValues();
        long res = 0;
        try {
            res = db.insertOrThrow(MessagesContract.MessagesEntry.TABLE_NAME, null, values);

            ContentValues ftsValues = new ContentValues();
            ftsValues.put(MessagesContract.MessagesEntry.COLUMN_NAME_MESSAGE_BODY, message.getMessageBody());
            ftsValues.put("docid", res);
            db.insertOrThrow("fts_" + MessagesContract.MessagesEntry.TABLE_NAME, null, ftsValues);

            if (message.getFiles() != null) {
                for (AttachedFile file : message.getFiles()) {
                    AttachmentsHelper.saveAttachment(file, db);
                }
            }
        } catch (Exception e) {
            Log.d(MessagesHelper.class.getSimpleName() + ".saveMessage", e.getMessage());
        }

        return res;
    }

    @NonNull
    private Cursor getMessagesCursor(@NonNull String cid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = MessagesContract.MessagesEntry.COLUMN_NAME_CID + " = ?";
        String[] selectionArgs = { cid };
        return db.query(MessagesContract.MessagesEntry.TABLE_NAME,
                MessagesContract.MESSAGE_PROJECTION,
                selection, selectionArgs,
                null, null, MessagesContract.MessagesEntry.COLUMN_NAME_MID + " ASC", null);
    }

    @NonNull
    public List<Message> getMessages(@NonNull String cid) {
        Cursor messagesCursor = getMessagesCursor(cid);
        ArrayList<Message> messagesList = new ArrayList<>(messagesCursor.getCount());

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        for(messagesCursor.moveToFirst();
            !messagesCursor.isAfterLast(); messagesCursor.moveToNext()) {
            try {
                Message message = new Message(messagesCursor);
                if (message.getMid() != null) {
                    message.setFiles(AttachmentsHelper.getAttachments(message.getMid(), db));
                }
                messagesList.add(message);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        messagesCursor.close();
        return messagesList;
    }

    public void updateMessageList(@NonNull List<Message> messageList, String cid) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long res;
        Log.d(MessagesHelper.class.getSimpleName() + ".updateMessageList", "updateMessageList");

        db.beginTransaction();
        try {
            Log.d(MessagesHelper.class.getSimpleName() + ".updateMessageList", "try deleteAll");
            deleteAll(db, cid);
            Log.d(MessagesHelper.class.getSimpleName() + ".updateMessageList", "try to saveMessage for " + messageList.size() + " elements");
            for (Message message : messageList) {
                message.setCid(cid);
                res = saveMessage(message);
                Log.d(MessagesHelper.class.getSimpleName() + ".updateMessageList", message.getMessageBody() + " -> " + res);
            }
            Log.d(MessagesHelper.class.getSimpleName() + ".updateMessageList", "done");
            db.setTransactionSuccessful();
        } catch(Exception e) {
            Log.d(MessagesHelper.class.getSimpleName() + ".updateMessageList", e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    private long deleteAll(SQLiteDatabase db, String cid) {
        int count = db.delete(
                MessagesContract.MessagesEntry.TABLE_NAME,
                MessagesContract.MessagesEntry.COLUMN_NAME_CID + " = ?",
                new String[] {cid}
        );
        db.execSQL("INSERT INTO fts_" + MessagesContract.MessagesEntry.TABLE_NAME +
                "(fts_" + MessagesContract.MessagesEntry.TABLE_NAME + ")" +
                " VALUES('rebuild')");
        return count;
    }
}
