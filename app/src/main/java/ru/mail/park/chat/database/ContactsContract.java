package ru.mail.park.chat.database;

import android.provider.BaseColumns;

public class ContactsContract {
    private static final String COMMA_SEP = ",";

    static final String CREATE_TABLE =
            "CREATE TABLE " + ContactsEntry.TABLE_NAME + " (" +
                    ContactsEntry.COLUMN_NAME_UID + " INT PRIMARY KEY NOT NULL" + COMMA_SEP +
                    ContactsEntry.COLUMN_NAME_LOGIN + " TEXT NOT NULL" + COMMA_SEP +
                    ContactsEntry.COLUMN_NAME_EMAIL + " TEXT" + COMMA_SEP +
                    ContactsEntry.COLUMN_NAME_PHONE + " TEXT" + COMMA_SEP +
                    ContactsEntry.COLUMN_NAME_FIRST_NAME + " TEXT" + COMMA_SEP +
                    ContactsEntry.COLUMN_NAME_LAST_NAME + " TEXT" + COMMA_SEP +
                    ContactsEntry.COLUMN_NAME_ABOUT + " Text" + COMMA_SEP +
                    ContactsEntry.COLUMN_NAME_PUBKEY + " BLOB" + COMMA_SEP +
                    ContactsEntry.COLUMN_NAME_ONIOIN + " TEXT" +
                    ")";

    static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + ContactsEntry.TABLE_NAME;

    static final String[] CONTACT_PROJECTION = {
            ContactsEntry.COLUMN_NAME_UID,
            ContactsEntry.COLUMN_NAME_LOGIN,

            ContactsEntry.COLUMN_NAME_EMAIL,
            ContactsEntry.COLUMN_NAME_PHONE,
            ContactsEntry.COLUMN_NAME_FIRST_NAME,
            ContactsEntry.COLUMN_NAME_LAST_NAME,

            ContactsEntry.COLUMN_NAME_PUBKEY,
            ContactsEntry.COLUMN_NAME_ONIOIN
    };

    public static final int PROJECTION_UID_INDEX = 0;
    public static final int PROJECTION_LOGIN_INDEX = 1;

    public static final int PROJECTION_EMAIL_INDEX = 2;
    public static final int PROJECTION_PHONE_INDEX = 3;
    public static final int PROJECTION_FIRST_NAME_INDEX = 4;
    public static final int PROJECTION_LAST_NAME_INDEX = 5;

    public static final int PROJECTION_PUBKEY_INDEX = 6;
    public static final int PROJECTION_ONION_INDEX = 7;

    public static abstract class ContactsEntry implements BaseColumns {
        public static final String TABLE_NAME = "contacts";
        public static final String COLUMN_NAME_UID = "uid";
        public static final String COLUMN_NAME_LOGIN = "login";

        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_PHONE = "phone";
        public static final String COLUMN_NAME_FIRST_NAME = "first_name";
        public static final String COLUMN_NAME_LAST_NAME = "last_name";
        public static final String COLUMN_NAME_ABOUT = "about";

        public static final String COLUMN_NAME_PUBKEY = "public_key";
        public static final String COLUMN_NAME_ONIOIN = "onion_address";
    }
}
