package ru.mail.park.chat.models;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.mail.park.chat.database.MessagesContract;
import ru.mail.park.chat.database.MessengerDBHelper;
import ru.mail.park.chat.database.PreferenceConstants;

/**
 * Created by Михаил on 26.03.2016.
 */
public class Message implements Comparable<Message> {
    private @Nullable String mid;
    private @NonNull String messageBody;
    private @NonNull String cid;
    private @NonNull String uid;
    private @Nullable Calendar date;

    private @NonNull String title;

    private Message(@NonNull String messageBody,
                    @NonNull String chatID,
                    @NonNull String userID) {
        this.messageBody = messageBody;
        cid = chatID;
        uid = userID;
    }

    public Message(@NonNull JSONObject message, @NonNull Context context) throws JSONException {
        this(message, context, null);
    }

    public Message(@NonNull JSONObject message, @NonNull Context context, String cid) throws JSONException {
        String uid;
        if (message.has("user")) {
            uid = String.valueOf(message.getJSONObject("user").getLong("id"));
        } else if (message.has("idUser")) {
            uid = String.valueOf(message.getInt("idUser"));
        } else {
            SharedPreferences pref =
                    context.getSharedPreferences(PreferenceConstants.PREFERENCE_NAME,
                            Context.MODE_PRIVATE);
            uid = pref.getString(PreferenceConstants.USER_UID_N, "");
        }

        String messageBodyParamName = null;

        if (message.has("textMessage"))
            messageBodyParamName = "textMessage";
        if (message.has("text"))
            messageBodyParamName = "text";
        if (messageBodyParamName != null) {
            messageBody = StringEscapeUtils.unescapeJava(message.getString(messageBodyParamName));
        }

        if (cid == null)
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
        } catch (ParseException e) {
            e.printStackTrace();
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
                isoDate = MessengerDBHelper.currentFormat.format(getDate().getTime());
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
              int mid1 = Integer.valueOf(mid);
              int mid2 = Integer.valueOf(another.mid);
            return mid1 - mid2;
          }
            return 1;
        }
        return -1;
    }
}
