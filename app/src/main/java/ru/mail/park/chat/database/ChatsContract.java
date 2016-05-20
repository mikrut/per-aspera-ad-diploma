package ru.mail.park.chat.database;

import android.provider.BaseColumns;

/**
 * Created by Михаил on 06.03.2016.
 */
public abstract class ChatsContract {
    private static final String COMMA_SEP = ",";

    static final String CREATE_TABLE =
            "CREATE TABLE " + ChatsEntry.TABLE_NAME + " (" +
            ChatsEntry.COLUMN_NAME_CID + " TEXT PRIMARY KEY NOT NULL" + COMMA_SEP +
            ChatsEntry.COLUMN_NAME_NAME + " TEXT NOT NULL" + COMMA_SEP +
            ChatsEntry.COLUMN_NAME_DESCRIPTION + " TEXT" + COMMA_SEP +
            ChatsEntry.COLUMN_NAME_COMPANION_ID + " TEXT" +
            ")";

    static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + ChatsEntry.TABLE_NAME;

    static final String[] CHAT_PROJECTION = {
            ChatsEntry.COLUMN_NAME_CID,
            ChatsEntry.COLUMN_NAME_NAME,
            ChatsEntry.COLUMN_NAME_DESCRIPTION
    };

    public static final int PROJECTION_CID_INDEX = 0;
    public static final int PROJECTION_NAME_INDEX = 1;
    public static final int PROJECTION_DESCRIPTION_INDEX = 2;

    public static abstract class ChatsEntry implements BaseColumns {
        public static final String TABLE_NAME = "chats";
        public static final String COLUMN_NAME_CID = "cid";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_COMPANION_ID = "companion_id";
    }
}
