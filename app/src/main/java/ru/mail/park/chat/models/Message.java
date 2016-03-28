package ru.mail.park.chat.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.mail.park.chat.database.MessagesContract;
import ru.mail.park.chat.database.MessengerDBHelper;

/**
 * Created by Михаил on 26.03.2016.
 */
public class Message implements Comparable<Message> {
    private @Nullable String mid;
    private @NonNull String messageBody;
    private @NonNull String cid;
    private @NonNull String uid;
    private @Nullable Calendar date;

    public Message(@NonNull String messageBody,
                   @NonNull String chatID,
                   @NonNull String userID) {
        this.messageBody = messageBody;
        cid = chatID;
        uid = userID;
    }

    public Message(@NonNull JSONObject message) throws JSONException, ParseException {
        this(message.getString("msg_body"),
                message.getString("cid"),
                String.valueOf(message.getLong("uid")));

        if (message.has("mid")) {
            setMid(message.getString("mid"));
        }

        if (message.has("dtCreate")) {
            String dateString = message.getString("dtCreate");
            setDate(dateString);
        }
    }
    
    public Message(@NonNull Cursor message) throws ParseException {
        this(message.getString(MessagesContract.PROJECTION_MESSAGE_BODY_INDEX),
                message.getString(MessagesContract.PROJECTION_CID_INDEX),
                message.getString(MessagesContract.PROJECTION_UID_INDEX));

        setMid(message.getString(MessagesContract.PROJECTION_MID_INDEX));
        String dateString = message.getString(MessagesContract.PROJECTION_DATETIME_INDEX);
        setDate(dateString);
    }

    @Nullable
    public String getMid() {
        return mid;
    }

    public void setMid(@NonNull String mid) {
        this.mid = mid;
    }

    @Nullable
    public Calendar getDate() {
        return date;
    }

    public void setDate(@Nullable String dateString) throws ParseException {
        java.util.Date date = MessengerDBHelper.iso8601.parse(dateString);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        setDate(calendar);
    }

    public void setDate(@Nullable Calendar date) {
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

    @Nullable
    public ContentValues getContentValues() {
        if (getMid() != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MessagesContract.MessagesEntry.COLUMN_NAME_MID, getMid());
            contentValues.put(MessagesContract.MessagesEntry.COLUMN_NAME_UID, getUid());
            contentValues.put(MessagesContract.MessagesEntry.COLUMN_NAME_CID, getCid());
            contentValues.put(MessagesContract.MessagesEntry.COLUMN_NAME_MESSAGE_BODY, getMessageBody());

            String isoDate = null;
            if (getDate() != null) {
                isoDate = MessengerDBHelper.iso8601.format(getDate().getTime());
            }
            contentValues.put(MessagesContract.MessagesEntry.COLUMN_NAME_DATETIME, isoDate);
            return contentValues;
        } else {
            return null;
        }
    }

    @Override
    public int compareTo(@NonNull Message another) {
        if (mid != null) {
          if (another.mid != null) {
            return mid.compareTo(another.mid);
          }
            return 1;
        }
        return -1;
    }
}
