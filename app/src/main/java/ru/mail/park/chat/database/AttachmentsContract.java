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
                    AttachmentsEntry.COLUMN_NAME_PATH + " TEXT NOT NULL" + COMMA_SEP +
                    AttachmentsEntry.COLUMN_NAME_DOWNLOADED + " INTEGER NOT NULL DEFAULT 0" + COMMA_SEP +
                    AttachmentsEntry.COLUMN_NAME_FSPATH + " TEXT" +
                    ")";

    static final String DROP_TABLE =
            "DROP TABLE IF EXISTS " + AttachmentsEntry.TABLE_NAME;

    static final String[] ATTACHMENT_PROJECTION = {
            AttachmentsEntry.COLUMN_NAME_FID,
            AttachmentsEntry.COLUMN_NAME_MID,
            AttachmentsEntry.COLUMN_NAME_TITLE,
            AttachmentsEntry.COLUMN_NAME_PATH,
            AttachmentsEntry.COLUMN_NAME_DOWNLOADED,
            AttachmentsEntry.COLUMN_NAME_FSPATH
    };

    public static final int PROJECTION_FID_INDEX = 0;
    public static final int PROJECTION_MID_INDEX = 1;
    public static final int PROJECTION_TITLE_INDEX = 2;
    public static final int PROJECTION_PATH_INDEX = 3;
    public static final int PROJECTION_DOWNLOADED_INDEX = 4;
    public static final int PROJECTION_FSPATH_INDEX = 5;


    public static abstract class AttachmentsEntry implements BaseColumns {
        public static final String TABLE_NAME = "attachments";

        public static final String COLUMN_NAME_MID = "mid";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_PATH = "path";
        public static final String COLUMN_NAME_FID = "fid";
        public static final String COLUMN_NAME_DOWNLOADED = "downloaded";
        public static final String COLUMN_NAME_FSPATH = "filename";
    }
}
