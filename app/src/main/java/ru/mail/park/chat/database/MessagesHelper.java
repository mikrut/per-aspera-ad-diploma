package ru.mail.park.chat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

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
        return db.insert(MessagesContract.MessagesEntry.TABLE_NAME, null, values);
    }

    public int deleteMessages(@NonNull String cid) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = MessagesContract.MessagesEntry.COLUMN_NAME_CID + " = ?";
        String[] selectionArgs = { cid };
        return db.delete(MessagesContract.MessagesEntry.TABLE_NAME,
                selection, selectionArgs);
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

        for(messagesCursor.moveToFirst();
            !messagesCursor.isAfterLast(); messagesCursor.moveToNext()) {
            try {
                messagesList.add(new Message(messagesCursor));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        messagesCursor.close();
        return messagesList;
    }
}
