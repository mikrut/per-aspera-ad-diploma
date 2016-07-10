package ru.mail.park.chat.models;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import ru.mail.park.chat.database.AttachmentsContract;

/**
 * Created by Михаил on 24.04.2016.
 */
public class AttachedFile {
    private String fileName;
    private String filePath;
    private long fileSize; // in bytes
    private String fileID;
    private String messageID;

    public AttachedFile(JSONObject file) throws JSONException {
        fileName = file.getString("path");
        filePath = fileName;
        if (file.has("id")) {
            fileID = String.valueOf(file.getInt("id"));
        }
        if (file.has("name")) {
            fileName = file.getString("name");
            if (file.has("format")) {
                fileName = fileName + "." + file.getString("format");
            }
        }
        // this.fileID = String.valueOf(file.getInt("id"));
    }

    public AttachedFile(Cursor cursor) {
        fileID = cursor.getString(AttachmentsContract.PROJECTION_FID_INDEX);
        messageID = cursor.getString(AttachmentsContract.PROJECTION_MID_INDEX);
        fileName = cursor.getString(AttachmentsContract.PROJECTION_TITLE_INDEX);
        filePath = cursor.getString(AttachmentsContract.PROJECTION_PATH_INDEX);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileID() {
        return fileID;
    }

    public String getFilePath() {
        return filePath;
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(AttachmentsContract.AttachmentsEntry.COLUMN_NAME_FID, fileID);
        contentValues.put(AttachmentsContract.AttachmentsEntry.COLUMN_NAME_MID, messageID);
        contentValues.put(AttachmentsContract.AttachmentsEntry.COLUMN_NAME_TITLE, fileName);
        contentValues.put(AttachmentsContract.AttachmentsEntry.COLUMN_NAME_PATH, filePath);

        return contentValues;
    }

    @Override
    public String toString() {
        return getFileID();
    }
}
