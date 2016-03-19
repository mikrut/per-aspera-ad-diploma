package ru.mail.park.chat.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.models.Chat;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */

// TODO: make actual requests
public class Chats extends ApiSection {
    private static final String URL_ADDITION = "chats/";

    @Override
    protected String getUrlAddition() {
        return super.getUrlAddition() + URL_ADDITION;
    }

    public Chats(@NonNull Context context) {
        super(context);
    }

    // TODO: think about chat creation, write code
    public void createChat() {
        final String requestURL = "dialogs";
        final String requestMethod = "PUT";

        JSONObject result = null;
    }

    @NonNull
    public List<Chat> getChats() throws IOException {
        final String requestURL = "dialogs";
        final String requestMethod = "GET";

        List<Chat> chatsList;
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod));
            final int status = result.getInt("status");
            if(status == 200) {
                JSONObject data = result.getJSONObject("data");
                JSONArray chats = data.getJSONArray("dialogs");

                chatsList = chatsFrom(chats);
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException | ParseException e) {
            throw new IOException("Server error");
        }

        return chatsList;
    }

    enum DialogPrivilege {USER, ADMIN};

    @NonNull
    public Chat addUser(@NonNull String cid, @NonNull String uid) throws IOException {
        return addUser(cid, uid, null);
    }

    @NonNull
    public Chat addUser(@NonNull String cid, @NonNull String uid,
                        @Nullable DialogPrivilege privilege) throws IOException {
        final String requestURL = "user";
        final String requestMethod = "POST";

        if (privilege != null) {
            // TODO: add privilege to request
        }

        Chat chat;
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod));
            final int status = result.getInt("status");
            if(status == 200) {
                JSONObject data = result.getJSONObject("data");
                JSONObject dialog = data.getJSONObject("dialog");

                chat = new Chat(dialog);
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            throw new IOException("Server error");
        }

        return chat;
    }

    @NonNull
    public Chat deleteUser(String cid, String uid) throws IOException {
        final String requestURL = "user";
        final String requestMethod = "DELETE";

        Chat chat;
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod));
            final int status = result.getInt("status");
            if(status == 200) {
                JSONObject data = result.getJSONObject("data");
                JSONObject dialog = data.getJSONObject("dialog");

                chat = new Chat(dialog);
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            throw new IOException("Server error");
        }

        return chat;
    }

    private static List<Chat> chatsFrom(JSONArray chats) throws JSONException, ParseException {
        List<Chat> chatsList = new ArrayList<>(chats.length());
        for (int i = 0; i < chats.length(); i++) {
            Chat contact = new Chat(chats.getJSONObject(i));
            chatsList.add(contact);
        }
        return chatsList;
    }
}
