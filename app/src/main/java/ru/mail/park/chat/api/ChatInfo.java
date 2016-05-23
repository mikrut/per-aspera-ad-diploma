package ru.mail.park.chat.api;

import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.mail.park.chat.database.MessengerDBHelper;
import ru.mail.park.chat.models.Contact;

/**
 * Created by 1запуск BeCompact on 16.05.2016.
 */
public class ChatInfo {
    public enum chatTypes {INDIVIDUAL, GROUP}
    @NonNull
    private String cid;
    @NonNull
    private String name;
    @NonNull
    private Calendar lastSeen;
    @NonNull
    private Contact userFirst;
    @NonNull
    private Contact userSecond;

    private String img;
    private chatTypes type;

    public ChatInfo(JSONObject jsonChatInfo, Context context) {
        try {
            cid = jsonChatInfo.getString("id");
            name = jsonChatInfo.getString("name");
            type = jsonChatInfo.getInt("typeChatRoom") == 0 ? chatTypes.INDIVIDUAL : chatTypes.GROUP;
            img = jsonChatInfo.getString("img");

            if (jsonChatInfo.has("lastSeen")) {
                java.util.Date dateLastSeen = MessengerDBHelper.currentFormat.parse(jsonChatInfo.getString("lastSeen"));
                GregorianCalendar lastSeen = new GregorianCalendar();
                lastSeen.setTime(dateLastSeen);
                this.lastSeen = lastSeen;
            }

            JSONArray users = jsonChatInfo.getJSONArray("listUser");

            userFirst = new Contact(users.getJSONObject(0), context);
            userSecond = new Contact(users.getJSONObject(1), context);
        } catch(JSONException e) {
            cid = null;
            name = null;
            userFirst = null;
            userSecond = null;
            img = null;
        } catch(ParseException e) {
            cid = null;
            name = null;
            userFirst = null;
            userSecond = null;
            img = null;
        }
    }

    public String getCid() {
        return cid;
    }

    public String getName() {
        return name;
    }

    public String getImg() {
        return img;
    }

    public chatTypes getType() {
        return type;
    }

    public Contact getFirst() {
        return userFirst;
    }

    public Calendar getLastSeen() {
        return lastSeen;
    }

    public Contact getSecond() {
        return userSecond;
    }
}
