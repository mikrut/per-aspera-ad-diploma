package ru.mail.park.chat.models;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import ru.mail.park.chat.activities.tasks.DownloadFileTask;
import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.database.AttachmentsContract;
import ru.mail.park.chat.database.AttachmentsHelper;

/**
 * Created by Михаил on 24.04.2016.
 */
public class AttachedFile {
    private String fileName;
    private String filePath;
    private long fileSize; // in bytes
    private String fileID;
    private String messageID;
    private boolean downloaded = false;
    private String fileSystemPath = null;

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
        downloaded = (cursor.getInt(AttachmentsContract.PROJECTION_DOWNLOADED_INDEX) != 0);
        fileSystemPath = cursor.getString(AttachmentsContract.PROJECTION_FSPATH_INDEX);
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

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public void setFileSystemPath(String fileSystemPath) {
        this.fileSystemPath = fileSystemPath;
    }

    /**
     *
     * @return A File object associated with this attachment if exists. Null is returned otherwise.
     */
    @Nullable
    public File getFromFileSystem(Context context) {
        if (downloaded && fileSystemPath != null) {
            File file = new File(fileSystemPath);
            if (file.exists() && file.isFile()) {
                return file;
            } else {
                downloaded = false;
                fileSystemPath = null;
                AttachmentsHelper helper = new AttachmentsHelper(context);
                helper.saveAttachment(this);
                helper.close();
            }
        }
        return null;
    }

    public void openInNewActivity(Context context) {
        File attachmentFile = getFromFileSystem(context);
        if (attachmentFile != null) {
            String extension = FilenameUtils.getExtension(attachmentFile.getPath());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(attachmentFile),mimeType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "No handler for this type of file.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void download(final Context context, @Nullable final TextView progressTextView) {
        final String path;
        File torchatDir = null;
        if (fileSystemPath != null) {
            path = fileSystemPath;
        } else {
            File externalDir = Environment.getExternalStorageDirectory();
            torchatDir = new File(externalDir, "TorChatDocuments");
            if (!torchatDir.exists()) {
                torchatDir.mkdir();
            }
            if (torchatDir.exists() && torchatDir.isDirectory()) {
                File attachmentFile = new File(torchatDir, fileName);
                if (attachmentFile.exists()) {
                    String extension = FilenameUtils.getExtension(attachmentFile.getPath());
                    String name = FilenameUtils.getBaseName(attachmentFile.getPath());
                    String hash = "";
                    try {
                        MessageDigest md5 = MessageDigest.getInstance("MD5");
                        byte[] hashingBase = ByteBuffer
                                .allocate(Long.SIZE + Integer.SIZE +
                                        fileID.length() + messageID.length())
                                .putLong(System.currentTimeMillis())
                                .putInt(hashCode())
                                .put(fileID.getBytes())
                                .put(messageID.getBytes())
                                .array();
                        hash = Base64.encodeToString(md5.digest(hashingBase), Base64.URL_SAFE | Base64.NO_WRAP);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        hash = String.valueOf(System.currentTimeMillis());
                    }

                    path = name + "--" + hash + "." + extension;
                } else {
                    path = attachmentFile.getPath();
                }
            } else {
                path = null;
            }

        }
        if (path != null) {
            File attachmentFile;
            try {
                if (fileSystemPath != null) {
                    attachmentFile = new File(path);
                } else {
                    attachmentFile = new File(torchatDir, path);
                    attachmentFile.createNewFile();

                }
                DownloadFileTask task = new DownloadFileTask(context, attachmentFile, this);
                task.setDownloadProgressListener(new DownloadFileTask.IDownloadProgressListener() {
                    @Override
                    public void onProgressUpdate(@NonNull DownloadFileTask.DownloadProgress progress) {
                        if (progressTextView != null) {
                            String progressText =
                                    AttachedFile.humanReadableByteCount(progress.totalWritten) +
                                    '/' +
                                    AttachedFile.humanReadableByteCount(progress.totalFileSize);
                            progressTextView.setText(progressText);
                            progressTextView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onPostExecute(boolean success) {
                        if (progressTextView != null) {
                            progressTextView.setText(success ? "Download success" : "Download failure");
                            progressTextView.setVisibility(View.VISIBLE);
                        }
                    }
                });
                try {
                    task.execute(new URL(ApiSection.SERVER_URL + filePath));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();

        contentValues.put(AttachmentsContract.AttachmentsEntry.COLUMN_NAME_FID, fileID);
        contentValues.put(AttachmentsContract.AttachmentsEntry.COLUMN_NAME_MID, messageID);
        contentValues.put(AttachmentsContract.AttachmentsEntry.COLUMN_NAME_TITLE, fileName);
        contentValues.put(AttachmentsContract.AttachmentsEntry.COLUMN_NAME_PATH, filePath);
        contentValues.put(AttachmentsContract.AttachmentsEntry.COLUMN_NAME_DOWNLOADED, downloaded ? 1 : 0);
        contentValues.put(AttachmentsContract.AttachmentsEntry.COLUMN_NAME_FSPATH, fileSystemPath);

        return contentValues;
    }

    @Override
    public String toString() {
        return getFileID();
    }

    public static String humanReadableByteCount(long bytes) {
        return humanReadableByteCount(bytes, true);
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
