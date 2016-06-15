package ru.mail.park.chat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Михаил on 06.03.2016.
 */
public class MessengerDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 7;
    private static final String DATABASE_NAME = "Messenger.db";

    public static final DateFormat currentFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    public static final DateFormat iso8086 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());

    public MessengerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ChatsContract.CREATE_TABLE);
        db.execSQL(ContactsContract.CREATE_TABLE);
        db.execSQL(MessagesContract.CREATE_TABLE);
        db.execSQL(MessagesContract.CREATE_FTS_TABLE);
        db.execSQL(AttachmentsContract.CREATE_TABLE);

        db.execSQL(ContactsToChatsContract.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropDatabase(db);
        onCreate(db);
    }

    private void dropDatabase(SQLiteDatabase db) {
        db.execSQL(ChatsContract.DROP_TABLE);
        db.execSQL(ContactsContract.DROP_TABLE);
        db.execSQL(MessagesContract.DROP_TABLE);
        db.execSQL(MessagesContract.DROP_FTS_TABLE);
        db.execSQL(AttachmentsContract.DROP_TABLE);

        db.execSQL(ContactsToChatsContract.DROP_TABLE);
    }

    public void clearDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(ChatsContract.ChatsEntry.TABLE_NAME, null, null);
            db.delete(ContactsContract.ContactsEntry.TABLE_NAME, null, null);
            db.delete(MessagesContract.MessagesEntry.TABLE_NAME, null, null);
            db.delete("fts_" + MessagesContract.MessagesEntry.TABLE_NAME, null, null);
            db.delete(AttachmentsContract.AttachmentsEntry.TABLE_NAME, null, null);
            db.delete(ContactsToChatsContract.ContactsToChatsEntry.TABLE_NAME, null, null);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
