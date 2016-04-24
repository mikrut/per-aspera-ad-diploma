package ru.mail.park.chat.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Михаил on 24.04.2016.
 */
public class AttachedFile {
    private String fileName;
    private String filePath;
    private long fileSize; // in bytes
    private String fileID;

    public AttachedFile(JSONObject file) throws JSONException {
        fileName = file.getString("path");
        filePath = fileName;
        if (file.has("id")) {
            fileID = file.getString("id");
        }
        if (file.has("name")) {
            fileName = file.getString("name");
        }
        // this.fileID = String.valueOf(file.getInt("id"));
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
}
