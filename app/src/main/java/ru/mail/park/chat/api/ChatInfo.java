package ru.mail.park.chat.api;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

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
    private Contact userFirst;
    @NonNull
    private Contact userSecond;

    private String img;
    private chatTypes type;

    public ChatInfo(JSONObject jsonChatInfo) {
        try {
            cid = jsonChatInfo.getString("id");
            name = jsonChatInfo.getString("name");
            type = jsonChatInfo.getInt("typeChatRoom") == 0 ? chatTypes.INDIVIDUAL : chatTypes.GROUP;
            img = jsonChatInfo.getString("img");

            JSONArray users = jsonChatInfo.getJSONArray("listUser");

            userFirst = new Contact(users.getJSONObject(0));
            userSecond = new Contact(users.getJSONObject(1));
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

    public Contact getSecond() {
        return userSecond;
    }
}
