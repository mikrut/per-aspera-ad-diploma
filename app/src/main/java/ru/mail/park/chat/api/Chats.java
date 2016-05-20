package ru.mail.park.chat.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Message;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */

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
    public List<Message> getMessages(String cid) throws IOException {
        final String requestURL = "messages";
        final String requestMethod = "POST";

        Log.d("[TP-diploma]", "inside getMessages");

        List<Pair<String, String>> parameters = new ArrayList<>(2);
        parameters.add(new Pair<>("idRoom", cid));

        List<Message> messagesList = new LinkedList<>();
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            Log.d("[TP-diploma]", result.toString());
            int status = 200;
            if (result.has("status"))
                status = result.getInt("status");
            if (status == 200) {
                // JSONObject data = result.getJSONObject("data");
                JSONArray messages = result.getJSONArray("data");

                for (int i = 0; i < messages.length(); i++) {
                    Message message = new Message(messages.getJSONObject(i), getContext(), cid);
                    messagesList.add(message);
                }
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            throw new IOException("Server error", e);
        }

        return messagesList;
    }

    @NonNull
    public List<Chat> getChats() throws IOException {
        final String requestURL = "list";
        final String requestMethod = "POST";

        List<Chat> chatsList;
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod));
            final int status = result.getInt("status");
            if(status == 200) {
                JSONArray chats = result.getJSONArray("data");

                chatsList = chatsFrom(chats);
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            throw new IOException("Server error", e);
        }

        return chatsList;
    }

    enum DialogPrivilege {
        USER, ADMIN;

        @Override
        public String toString() {
            switch (this) {
                case USER:
                    return "USER";
                case ADMIN:
                    return "ADMIN";
                default:
                    return super.toString();
            }
        }
    }

    @NonNull
    public Chat addUser(@NonNull String cid, @NonNull String uid) throws IOException {
        return addUser(cid, uid, null);
    }

    @NonNull
    public ChatInfo getChatInfo(String cid) throws IOException {
        ChatInfo chatInfo;
        final String requestURL = "get";
        final String requestMethod = "POST";

        List<Pair<String, String>> parameters = new ArrayList<>(2);
        parameters.add(new Pair<>("idRoom", cid));

        try {
            String response = executeRequest(requestURL, requestMethod, parameters);
            Log.d("[TP-diploma]", "getChatInfo(" + cid + ") result: " + response);
            JSONObject result = new JSONObject(response);
            final int status = result.getInt("status");
            if(status == 200) {
                JSONObject ci = result.getJSONObject("data");
                chatInfo = new ChatInfo(ci, getContext());
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            Log.d("[TP-diploma]", e.getMessage());
            throw new IOException("Server error", e);
        }

        return chatInfo;
    }

    @NonNull
    public Chat addUser(@NonNull String cid, @NonNull String uid,
                         @Nullable DialogPrivilege privilege) throws IOException {
        final String requestURL = "user";
        final String requestMethod = "POST";

        List<Pair<String, String>> parameters = new ArrayList<>(3 + (privilege != null ? 1 : 0));
        parameters.add(new Pair<>("cid", cid));
        parameters.add(new Pair<>("uid", uid));
        if (privilege != null) {
            parameters.add(new Pair<>("grant", privilege.toString()));
        }

        Chat chat;
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            final int status = result.getInt("status");
            if(status == 200) {
                JSONObject data = result.getJSONObject("data");
                JSONObject dialog = data.getJSONObject("dialog");

                chat = new Chat(dialog, getContext());
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

        List<Pair<String, String>> parameters = new ArrayList<>(3);
        parameters.add(new Pair<>("cid", cid));
        parameters.add(new Pair<>("uid", uid));

        Chat chat;
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            final int status = result.getInt("status");
            if(status == 200) {
                JSONObject data = result.getJSONObject("data");
                JSONObject dialog = data.getJSONObject("dialog");

                chat = new Chat(dialog, getContext());
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            throw new IOException("Server error");
        }

        return chat;
    }

    private List<Chat> chatsFrom(JSONArray chats) throws JSONException {
        List<Chat> chatsList = new ArrayList<>(chats.length());
        for (int i = 0; i < chats.length(); i++) {
            Chat contact = new Chat(chats.getJSONObject(i), getContext());
            chatsList.add(contact);
        }
        return chatsList;
    }
}
