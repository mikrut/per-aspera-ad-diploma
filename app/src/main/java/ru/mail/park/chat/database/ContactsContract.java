package ru.mail.park.chat.database;

import android.provider.BaseColumns;

public class ContactsContract {
    private static final String COMMA_SEP = ",";

    static final String CREATE_TABLE =
            "CREATE TABLE " + ContactsEntry.TABLE_NAME + " (" +
                    ContactsEntry.COLUMN_NAME_UID + " INT PRIMARY KEY NOT NULL" + COMMA_SEP +
                    ContactsEntry.COLUMN_NAME_LOGIN + " TEXT NOT NULL" + COMMA_SEP +
                    ContactsEntry.COLUMN_NAME_EMAIL + " TEXT NOT NULL" +
                    ")";

    static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + ContactsEntry.TABLE_NAME;

    static final String[] CONTACT_PROJECTION = {
            ContactsEntry.COLUMN_NAME_UID,
            ContactsEntry.COLUMN_NAME_LOGIN,
            ContactsEntry.COLUMN_NAME_EMAIL
    };

    public static final int PROJECTION_UID_INDEX = 0;
    public static final int PROJECTION_LOGIN_INDEX = 1;
    public static final int PROJECTION_EMAIL_INDEX = 2;

    public static abstract class ContactsEntry implements BaseColumns {
        public static final String TABLE_NAME = "contacs";
        public static final String COLUMN_NAME_UID = "uid";
        public static final String COLUMN_NAME_LOGIN = "login";
        public static final String COLUMN_NAME_EMAIL = "email";
    }
}
