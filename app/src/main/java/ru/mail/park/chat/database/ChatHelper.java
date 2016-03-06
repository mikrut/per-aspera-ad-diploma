package ru.mail.park.chat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.CursorAdapter;

import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.models.Chat;

/**
 * Created by Михаил on 06.03.2016.
 */
public class ChatHelper {
    private MessengerDBHelper dbHelper;
    private Context context;

    public ChatHelper(Context context) {
        this.context = context;
        dbHelper = new MessengerDBHelper(context);
    }

    public void updateChat(@NonNull Chat chat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = chat.getContentValues();
        String whereClause = ChatsContract.ChatsEntry.COLUMN_NAME_CID + " = ?";
        String[] whereArgs = {chat.getCid()};
        db.update(ChatsContract.ChatsEntry.TABLE_NAME, values, whereClause, whereArgs);
    }

    public long saveChat(@NonNull Chat chat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = chat.getContentValues();
        long result = db.insert(ChatsContract.ChatsEntry.TABLE_NAME, null, values);
        return result;
    }

    // FIXME: ORDER BY last_message_time DESC
    @NonNull
    public Cursor getChatsCursor() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        return db.query(
                ChatsContract.ChatsEntry.TABLE_NAME,
                ChatsContract.CHAT_PROJECTION,
                null, // Return all chats (no WHERE)
                null, // No WHERE - no args
                null, // No GROUP BY
                null, // No groupby filter
                null  // No ORDER BY
        );
    }

    @Nullable
    public Chat getChat(@NonNull String cid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = ChatsContract.ChatsEntry.COLUMN_NAME_CID + " = ?";
        String[] selectionArgs = { cid };
        Cursor cursor = db.query(ChatsContract.ChatsEntry.TABLE_NAME,
                ChatsContract.CHAT_PROJECTION,
                selection, selectionArgs,
                null, null, null,
                "1");

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Chat chat = new Chat(cursor);
            return chat;
        } else {
            return null;
        }
    }

    @NonNull
    public List<Chat> getChatsList() {
        Cursor chatsCursor = getChatsCursor();
        ArrayList<Chat> chatsList = new ArrayList<Chat>(chatsCursor.getCount());

        for(chatsCursor.moveToFirst(); chatsCursor.isAfterLast(); chatsCursor.moveToNext()) {
            chatsList.add(new Chat(chatsCursor));
        }
        return chatsList;
    }

    public int deleteChat(@NonNull String cid) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = ChatsContract.ChatsEntry.COLUMN_NAME_CID + " = ?";
        String[] selectionArgs = { cid };
        return db.delete(ChatsContract.ChatsEntry.TABLE_NAME, selection, selectionArgs);
    }
}
