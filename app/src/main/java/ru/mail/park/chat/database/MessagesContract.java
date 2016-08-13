package ru.mail.park.chat.database;

import android.provider.BaseColumns;

/**
 * Created by Михаил on 26.03.2016.
 */
public abstract class MessagesContract {
    private static final String COMMA_SEP = ",";

    static final String CREATE_TABLE =
            "CREATE TABLE " + MessagesEntry.TABLE_NAME + " (" +
                    MessagesEntry._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    MessagesEntry.COLUMN_NAME_MID + " TEXT NOT NULL" + COMMA_SEP +
                    MessagesEntry.COLUMN_NAME_CID + " TEXT NOT NULL" + COMMA_SEP +
                    MessagesEntry.COLUMN_NAME_UID + " TEXT NOT NULL" + COMMA_SEP +
                    MessagesEntry.COLUMN_NAME_MESSAGE_BODY + " TEXT NOT NULL" + COMMA_SEP +
                    MessagesEntry.COLUMN_NAME_DATETIME + " TEXT" + COMMA_SEP +
                    MessagesEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL" + COMMA_SEP +
                    MessagesEntry.COLUMN_NAME_IMAGE_URL + " TEXT" +
                    ")";
    static final String CREATE_FTS_TABLE = "" +
            "CREATE VIRTUAL TABLE fts_" + MessagesEntry.TABLE_NAME +
            " USING fts4 (content=\'" + MessagesEntry.TABLE_NAME + "\', " +
            MessagesEntry.COLUMN_NAME_MESSAGE_BODY + ")";

    static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + MessagesEntry.TABLE_NAME;
    static final String DROP_FTS_TABLE =
            "DROP TABLE IF EXISTS fts_" + MessagesEntry.TABLE_NAME;

    static final String[] MESSAGE_PROJECTION = {
            MessagesEntry.COLUMN_NAME_MID,
            MessagesEntry.COLUMN_NAME_CID,
            MessagesEntry.COLUMN_NAME_UID,
            MessagesEntry.COLUMN_NAME_MESSAGE_BODY,
            MessagesEntry.COLUMN_NAME_DATETIME,
            MessagesEntry.COLUMN_NAME_TITLE,
            MessagesEntry.COLUMN_NAME_IMAGE_URL
    };

    public static final int PROJECTION_MID_INDEX = 0;
    public static final int PROJECTION_CID_INDEX = 1;
    public static final int PROJECTION_UID_INDEX = 2;
    public static final int PROJECTION_MESSAGE_BODY_INDEX = 3;
    public static final int PROJECTION_DATETIME_INDEX = 4;
    public static final int PROJECTION_TITLE_INDEX = 5;
    public static final int PROJECTION_IMAGE_URL_INDEX = 6;

    public static abstract class MessagesEntry implements BaseColumns {
        public static final String TABLE_NAME = "messages";
        public static final String COLUMN_NAME_MID = "mid";
        public static final String COLUMN_NAME_CID = "cid";
        public static final String COLUMN_NAME_UID = "uid";
        public static final String COLUMN_NAME_MESSAGE_BODY = "message_body";
        public static final String COLUMN_NAME_DATETIME = "datetime";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_IMAGE_URL = "imageURL";
    }
}
