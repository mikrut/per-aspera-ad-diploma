package ru.mail.park.chat.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 21.05.2016.
 */
public class ContactsToChatsHelper {
    private final MessengerDBHelper dbHelper;

    private static final String usersSelection;
    private static final String chatsSelection;
    private final static String linkTable = ContactsToChatsContract.ContactsToChatsEntry.TABLE_NAME;
    private final static String contactTable = ContactsContract.ContactsEntry.TABLE_NAME;
    private final static String chatTable = ChatsContract.ChatsEntry.TABLE_NAME;

    static {
        usersSelection = selectionForProjection(contactTable, ContactsContract.CONTACT_PROJECTION)
            + ", " + selectionForProjection(linkTable, ContactsToChatsContract.LINK_PROJECTION);

        chatsSelection = selectionForProjection(chatTable, ChatsContract.CHAT_PROJECTION) + ", " +
                selectionForProjection(linkTable, ContactsToChatsContract.LINK_PROJECTION);
    }

    private static String selectionForProjection(String table, String[] projection) {
        StringBuilder queryBuilder = new StringBuilder();
        for (int i = 0; i < projection.length; i++) {
            queryBuilder.append(table + "." + projection[i]);
            if (i != projection.length - 1)
                queryBuilder.append(", ");
        }
        return queryBuilder.toString();
    }

    public ContactsToChatsHelper(Context context) {
        this.dbHelper = new MessengerDBHelper(context);
    }

    private static Cursor getChatUsersCursor(@NonNull String cid, @NonNull SQLiteDatabase db) {
        String rawQuery = "SELECT " + usersSelection +
                " FROM " + linkTable + " JOIN " + contactTable +
                " ON " + linkTable + "." + ContactsToChatsContract.ContactsToChatsEntry.COLUMN_NAME_UID +
                " = " + contactTable + "." + ContactsContract.ContactsEntry.COLUMN_NAME_UID +
                " WHERE " + linkTable + "." + ContactsToChatsContract.ContactsToChatsEntry.COLUMN_NAME_CID +
                " = ?";
        String[] queryArgs = {cid};

        //return
        Cursor result = db.rawQuery(
                rawQuery,
                queryArgs
        );

        Cursor cur2 = db.rawQuery(
                "SELECT " + ContactsToChatsContract.ContactsToChatsEntry.COLUMN_NAME_UID + " FROM "
                        + ContactsToChatsContract.ContactsToChatsEntry.TABLE_NAME +
                        " WHERE " + linkTable + "." + ContactsToChatsContract.ContactsToChatsEntry.COLUMN_NAME_CID +
                        " = ?",
                queryArgs
        );

        for (cur2.moveToFirst(); !cur2.isAfterLast(); cur2.moveToNext()) {
            Log.d("UID", cur2.getString(0));
        }

        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
            if (result.isNull(0))
            Log.d("UID =", "null");
            else
            Log.d("UID =", String.valueOf(result.getInt(0)));
        }

        return result;
    }

    public static List<Contact> getChatUsers(@NonNull String cid, @NonNull SQLiteDatabase db) {
        List<Contact> contacts = new ArrayList<>();

        Cursor cursor = getChatUsersCursor(cid, db);
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Contact contact = new Contact(cursor);
            contacts.add(contact);
        }

        cursor.close();
        return contacts;
    }

    private static int deleteChatUsers(@NonNull String cid, @NonNull SQLiteDatabase db) {
        final String selection = ContactsToChatsContract.ContactsToChatsEntry.COLUMN_NAME_CID + " = ?";
        final String[] whereArgs = { cid };

        return db.delete(linkTable, selection, whereArgs);
    }

    public static void saveChatUsers(@NonNull String cid, @NonNull List<Contact> contacts,
                                     @NonNull SQLiteDatabase db) {
        deleteChatUsers(cid, db);
        for (Contact contact : contacts) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ContactsToChatsContract.ContactsToChatsEntry.COLUMN_NAME_CID, cid);
            contentValues.put(ContactsToChatsContract.ContactsToChatsEntry.COLUMN_NAME_UID, contact.getUid());
            db.insert(linkTable, null, contentValues);
        }
    }

    @Nullable
    public Chat getChat(@NonNull String uid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String rawQuery = "SELECT " + chatsSelection +
                " FROM " + linkTable + " LEFT JOIN " + chatTable +
                " ON " + linkTable + "." + ContactsToChatsContract.ContactsToChatsEntry.COLUMN_NAME_CID +
                " = " + chatTable + "." + ChatsContract.ChatsEntry.COLUMN_NAME_CID +
                " WHERE " + linkTable + "." + ContactsToChatsContract.ContactsToChatsEntry.COLUMN_NAME_UID + " = ?" +
                " AND " + chatTable + "." + ChatsContract.ChatsEntry.COLUMN_NAME_TYPE + " = " + Chat.INDIVIDUAL_TYPE;
        String[] queryArgs = {uid};

        Cursor cursor = db.rawQuery(
                rawQuery,
                queryArgs
        );

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            Chat chat = new Chat(cursor);
            chat.setChatUsers(getChatUsers(chat.getCid(), db));
            return chat;
        }
        return null;
    }

    public List<Contact> getContacts(@NonNull String cid) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<Contact> contacts = getChatUsers(cid, db);
        db.close();
        return contacts;
    }
}
