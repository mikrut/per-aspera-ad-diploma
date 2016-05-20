package ru.mail.park.chat.database;

import android.provider.BaseColumns;

/**
 * Created by mikrut on 20.05.16.
 */
public class AttachmentsContract {
    private static final String COMMA_SEP = ",";

    static final String CREATE_TABLE =
            "CREATE TABLE " + AttachmentsEntry.TABLE_NAME + " (" +
                    AttachmentsEntry.COLUMN_NAME_FID + " TEXT PRIMARY KEY NOT NULL" + COMMA_SEP +
                    AttachmentsEntry.COLUMN_NAME_MID + " TEXT NOT NULL" + COMMA_SEP +
                    AttachmentsEntry.COLUMN_NAME_TITLE + " TEXT" + COMMA_SEP +
                    AttachmentsEntry.COLUMN_NAME_PATH + " TEXT NOT NULL" +
                    ")";

    static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + AttachmentsEntry.TABLE_NAME;

    static final String[] ATTACHMENT_PROJECTION = {
            AttachmentsEntry.COLUMN_NAME_FID,
            AttachmentsEntry.COLUMN_NAME_MID,
            AttachmentsEntry.COLUMN_NAME_TITLE,
            AttachmentsEntry.COLUMN_NAME_PATH
    };

    public static final int PROJECTION_FID_INDEX = 0;
    public static final int PROJECTION_MID_INDEX = 1;
    public static final int PROJECTION_TITLE_INDEX = 2;
    public static final int PROJECTION_PATH_INDEX = 3;


    public static abstract class AttachmentsEntry implements BaseColumns {
        public static final String TABLE_NAME = "attachments";
        public static final String COLUMN_NAME_MID = "mid";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String COLUMN_NAME_FID = "fid";
    }
}
