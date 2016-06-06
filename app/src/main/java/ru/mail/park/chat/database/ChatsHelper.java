package ru.mail.park.chat.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 06.03.2016.
 */
public class ChatsHelper {
    private final MessengerDBHelper dbHelper;

    public static final String LOG_TAG = "[TP-diploma]";

    public ChatsHelper(Context context) {
        dbHelper = new MessengerDBHelper(context);
    }

    public long saveChat(@NonNull Chat chat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = chat.getContentValues();
        ContactsToChatsHelper.saveChatUsers(chat.getCid(), chat.getChatUsers(), db);
        return db.insertWithOnConflict(ChatsContract.ChatsEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // FIXME: ORDER BY last_message_time DESC
    @NonNull
    private Cursor getChatsCursor(@NonNull String queryString, @NonNull SQLiteDatabase db) {
        queryString = queryString.replace("\\", "\\\\");
        queryString = queryString.replace("%", "\\%");
        queryString = queryString.replace("_", "\\_");
        queryString = queryString.replace("[", "\\[");

        String[] selectionArgs = { queryString + "*", "%" + queryString + "%" };

        return db.rawQuery("SELECT * FROM " + ChatsContract.ChatsEntry.TABLE_NAME +
                " WHERE " + ChatsContract.ChatsEntry.COLUMN_NAME_CID + " IN (" +
                "SELECT " + MessagesContract.MessagesEntry.COLUMN_NAME_CID +
                " FROM " + MessagesContract.MessagesEntry.TABLE_NAME + " WHERE " + MessagesContract.MessagesEntry._ID + " IN (" +
                "SELECT docid FROM fts_" + MessagesContract.MessagesEntry.TABLE_NAME + " WHERE fts_" + MessagesContract.MessagesEntry.TABLE_NAME + " MATCH ?" +
                ")" +
                ") OR " +
                ChatsContract.ChatsEntry.COLUMN_NAME_NAME + " LIKE ? ESCAPE '\\'", selectionArgs);
    }

    @NonNull
    private Cursor getChatsCursor(@NonNull SQLiteDatabase db) {
        String orderBy = ChatsContract.ChatsEntry.COLUMN_NAME_DATETIME + " ASC";

        return db.query(
                ChatsContract.ChatsEntry.TABLE_NAME,
                ChatsContract.CHAT_PROJECTION,
                null, // Return all chats (no WHERE)
                null, // No WHERE - no args
                null, // No GROUP BY
                null, // No GROUP BY filter
                orderBy
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

        Chat chat = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            chat = new Chat(cursor);
            List<Contact> contacts = ContactsToChatsHelper.getChatUsers(cid, db);
            chat.setChatUsers(contacts);
        }
        cursor.close();
        return chat;
    }

    @NonNull
    public List<Chat> getChatsList() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor chatsCursor = getChatsCursor(db);
        return cursorToList(chatsCursor, db);
    }

    @NonNull
    public List<Chat> getChatsList(@NonNull String queryString) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor chatsCursor = getChatsCursor(queryString, db);
        List<Chat> result = cursorToList(chatsCursor, db);
        chatsCursor.close();
        return result;
    }

    private static List<Chat> cursorToList(Cursor chatsCursor, SQLiteDatabase db) {
        ArrayList<Chat> chatsList = new ArrayList<>(chatsCursor.getCount());

        for(chatsCursor.moveToFirst(); !chatsCursor.isAfterLast(); chatsCursor.moveToNext()) {
            Chat chat = new Chat(chatsCursor);
            chat.setChatUsers(ContactsToChatsHelper.getChatUsers(chat.getCid(), db));
            chatsList.add(chat);
        }

        return chatsList;
    }

    public int deleteChat(@NonNull String cid) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = ChatsContract.ChatsEntry.COLUMN_NAME_CID + " = ?";
        String[] selectionArgs = { cid };
        return db.delete(ChatsContract.ChatsEntry.TABLE_NAME, selection, selectionArgs);
    }

    public void updateChatList(@NonNull List<Chat> chatList) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();
        try {
            for (Chat chat : chatList) {
                saveChat(chat);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private long deleteAll(SQLiteDatabase db) {
        try {
            return db.delete(ChatsContract.ChatsEntry.TABLE_NAME, null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void close() {
        dbHelper.close();
    }
}
