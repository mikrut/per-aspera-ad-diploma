package ru.mail.park.chat.database;

import android.provider.BaseColumns;

/**
 * Created by Михаил on 21.05.2016.
 */
public class ContactsToChatsContract {
    private static final String COMMA_SEP = ",";

    static final String CREATE_TABLE =
            "CREATE TABLE " + ContactsToChatsEntry.TABLE_NAME + " (" +
                    ContactsToChatsEntry.COLUMN_NAME_UID + " INT NOT NULL" + COMMA_SEP +
                    ContactsToChatsEntry.COLUMN_NAME_CID + " TEXT NOT NULL" +
                    ")";

    static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + ContactsToChatsEntry.TABLE_NAME;

    static final String[] LINK_PROJECTION = {
            ContactsToChatsEntry.COLUMN_NAME_UID,
            ContactsToChatsEntry.COLUMN_NAME_CID
    };

    public static final int PROJECTION_UID_INDEX = 0;
    public static final int PROJECTION_CID_INDEX = 1;

    public static abstract class ContactsToChatsEntry implements BaseColumns {
        public static final String TABLE_NAME = "contacts_to_chats";
        public static final String COLUMN_NAME_UID = "uid";
        public static final String COLUMN_NAME_CID = "cid";
    }
}
