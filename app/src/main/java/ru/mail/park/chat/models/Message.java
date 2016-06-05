package ru.mail.park.chat.models;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import ru.mail.park.chat.database.MessagesContract;
import ru.mail.park.chat.database.MessengerDBHelper;
import ru.mail.park.chat.database.PreferenceConstants;

/**
 * Created by Михаил on 26.03.2016.
 */
public class Message implements Comparable<Message> {
    private @Nullable String mid;
    private @NonNull String messageBody;
    private @Nullable String cid;
    private @NonNull String uid;
    private @Nullable Calendar date;

    private @NonNull String title;

    private long uniqueID;
    private List<AttachedFile> files = new ArrayList<AttachedFile>();

    private @Nullable String imageURL;

    public Message(String messageBody, Context context) {
        this.messageBody = messageBody;
        OwnerProfile owner = new OwnerProfile(context);
        uid = owner.getUid();
        title = owner.getContactTitle();
    }

    public Message(@NonNull JSONObject message, @NonNull Context context, @Nullable String cid) throws JSONException {
        String uid;
        if (message.has("user")) {
            uid = String.valueOf(message.getJSONObject("user").getLong("id"));
        } else if (message.has("idUser")) {
            uid = String.valueOf(message.getInt("idUser"));
        } else {
            OwnerProfile owner = new OwnerProfile(context);
            uid = owner.getUid();
        }

        String messageBodyParamName = null;

        if (message.has("textMessage"))
            messageBodyParamName = "textMessage";
        if (message.has("text"))
            messageBodyParamName = "text";
        if (messageBodyParamName != null) {
            messageBody = StringEscapeUtils.unescapeJava(message.getString(messageBodyParamName));
        } else {
            throw new JSONException("No textMessage or text parameter is JSON");
        }

        if (message.has("uniqueId")) {
            uniqueID = message.getLong("uniqueId");
        }

        if (cid == null && message.has("idRoom"))
            this.cid = message.getString("idRoom");
        this.uid = uid;

        if (message.has("idMessage")) {
            setMid(message.getString("idMessage"));
        } else if (message.has("id")) {
            setMid(message.getString("id"));
        }

        try {
            JSONObject userJSON = message;
            if (message.has("user")) {
                userJSON = message.getJSONObject("user");
            }
            Contact user = new Contact(userJSON, context);
            title = user.getContactTitle();
            imageURL = user.getImg();
        } catch (ParseException e) {
            e.printStackTrace();
            throw new JSONException("Invalid USER field in JSON for message");
        }

        String dtCreateParamName = null;
        if (message.has("dtCreateMessage"))
            dtCreateParamName = "dtCreateMessage";
        if (message.has("dtCreate"))
            dtCreateParamName = "dtCreate";
        if (dtCreateParamName != null) {
            String dateString = message.getString(dtCreateParamName);
            try {
                setDate(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (message.has("files")) {
            JSONArray filesArray = message.getJSONArray("files");
            for (int i = 0; i < filesArray.length(); i++) {
                AttachedFile file = new AttachedFile(filesArray.getJSONObject(i));
                file.setMessageID(mid);
                files.add(file);
            }
        }
    }
    
    public Message(@NonNull Cursor message) throws ParseException {
        messageBody = message.getString(MessagesContract.PROJECTION_MESSAGE_BODY_INDEX);
        cid = message.getString(MessagesContract.PROJECTION_CID_INDEX);
        uid = message.getString(MessagesContract.PROJECTION_UID_INDEX);
        title = message.getString(MessagesContract.PROJECTION_TITLE_INDEX);

        mid = message.getString(MessagesContract.PROJECTION_MID_INDEX);
        String dateString = message.getString(MessagesContract.PROJECTION_DATETIME_INDEX);
        setDate(dateString);
    }

    @Nullable
    public String getMid() {
        return mid;
    }

    private void setMid(@NonNull String mid) {
        this.mid = mid;
    }

    @Nullable
    private Calendar getDate() {
        return date;
    }

    private void setDate(@Nullable String dateString) throws ParseException {
        java.util.Date date = MessengerDBHelper.currentFormat.parse(dateString);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        setDate(calendar);
    }

    private void setDate(@Nullable Calendar date) {
        this.date = date;
    }

    @NonNull
    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(@NonNull String messageBody) {
        this.messageBody = messageBody;
    }

    @NonNull
    public String getCid() {
        return cid;
    }

    public void setCid(@NonNull String cid) {
        this.cid = cid;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public List<AttachedFile> getFiles() {
        return files;
    }

    @Nullable
    public ContentValues getContentValues() {
        if (getMid() != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MessagesContract.MessagesEntry.COLUMN_NAME_MID, getMid());
            contentValues.put(MessagesContract.MessagesEntry.COLUMN_NAME_UID, getUid());
            contentValues.put(MessagesContract.MessagesEntry.COLUMN_NAME_CID, getCid());
            contentValues.put(MessagesContract.MessagesEntry.COLUMN_NAME_MESSAGE_BODY, getMessageBody());
            contentValues.put(MessagesContract.MessagesEntry.COLUMN_NAME_TITLE, getTitle());

            String isoDate = null;
            if (getDate() != null) {
                isoDate = MessengerDBHelper.currentFormat.format(getDate().getTime());
            }
            contentValues.put(MessagesContract.MessagesEntry.COLUMN_NAME_DATETIME, isoDate);
            return contentValues;
        } else {
            return null;
        }
    }

    public void setFiles(List<AttachedFile> files) {
        this.files = files;
        if (files != null) {
            for (AttachedFile file : files) {
                if (file.getMessageID() == null)
                    file.setMessageID(mid);
            }
        }
    }

    public boolean isAcknowledged() {
        return mid != null;
    }

    public long getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(long uniqueID) {
        this.uniqueID = uniqueID;
    }

    @Nullable
    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(@Nullable String imageURL) {
        this.imageURL = imageURL;
    }

    @Override
    public int compareTo(@NonNull Message another) {
        if (mid != null) {
            if (another.mid != null) {
                long mid1 = Long.valueOf(mid);
                long mid2 = Long.valueOf(another.mid);
                return (int) ((mid1 - mid2) % Integer.MAX_VALUE);
            } else if (messageBody.equals(another.messageBody)) {
                return 0;
            }

            return 1;
        } else if (ObjectUtils.compare(uniqueID, another.uniqueID) == 0) {
            return 0;
        }
        return 1;
    }
}
