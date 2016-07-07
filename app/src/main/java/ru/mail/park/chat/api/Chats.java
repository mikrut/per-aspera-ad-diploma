package ru.mail.park.chat.api;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import org.apache.commons.lang3.ObjectUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ru.mail.park.chat.models.AttachedFile;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */

public class Chats extends ApiSection {
    private static final String URL_ADDITION = "chats";

    @Override
    protected Uri getUrlAddition() {
        return super.getUrlAddition().buildUpon().appendPath(URL_ADDITION).build();
    }

    public Chats(@NonNull Context context) {
        super(context);
    }

    @NonNull
    public List<Message> getMessages(String cid, @Nullable String lastReceivedMID) throws IOException {
        final String requestURL = "messages";
        final String requestMethod = "POST";

        Log.d("[TP-diploma]", "inside getMessages");

        List<Pair<String, String>> parameters = new ArrayList<>(3);
        parameters.add(new Pair<>("idRoom", cid));
        if (lastReceivedMID != null)
            parameters.add(new Pair<>("idMessage", lastReceivedMID));

        List<Message> messagesList = new LinkedList<>();
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            Log.d("[TP-diploma]", result.toString());
            int status = 200;
            if (result.has("status"))
                status = result.getInt("status");
            if (status == 200) {
                JSONObject data = result.getJSONObject("data");
                JSONArray messages = data.getJSONArray("listMessages");

                for (int i = 0; i < messages.length(); i++) {
                    Message message = new Message(messages.getJSONObject(i), getContext(), cid);
                    messagesList.add(message);
                }
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException | NullPointerException e) {
            throw new IOException("Server error", e);
        }

        return messagesList;
    }

    @NonNull
    @Deprecated
    public List<Chat> getChats() throws IOException {
        return getChats(1);
    }

    @NonNull
    public List<Chat> getChats(int page) throws IOException {
        final String requestURL = "list";
        final String requestMethod = "POST";

        List<Pair<String, String>> parameters = new ArrayList<>(2);
        parameters.add(new Pair<>("page", String.valueOf(page)));

        List<Chat> chatsList;
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            final int status = result.getInt("status");
            if (status == 200) {
                JSONArray chats = result.getJSONObject("data").getJSONArray("listChats");
                chatsList = chatsFrom(chats);
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (NullPointerException | JSONException e) {
            throw new IOException("Server error", e);
        }

        return chatsList;
    }

    @NonNull
    public Chat getChatInfo(String cid) throws IOException {
        Chat chat;
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
                chat = new Chat(ci, getContext());
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            Log.d("[TP-diploma]", e.getMessage());
            throw new IOException("Server error", e);
        }

        return chat;
    }

    public class ImageUpdateResult {
        public Contact user;
        public URL image;
    }

    @NonNull
    public ImageUpdateResult updateImage(String cid, File image) throws IOException {
        final String requestURL = "updateImg";
        final String requestMethod = "POST";

        List<Pair<String, Object>> parameters = new ArrayList<>(2);
        parameters.add(new Pair<String, Object>("idRoom", cid));
        parameters.add(new Pair<String, Object>("img", image));

        try {
            String response = executeRequest(requestURL, requestMethod, parameters);

            JSONObject result = new JSONObject(response);
            final int status = result.getInt("status");
            if(status == 200) {
                JSONObject data = result;//result.getJSONObject("data");
                JSONObject user = data.getJSONObject("user");
                JSONObject img = data.getJSONObject("img");

                ImageUpdateResult retval = new ImageUpdateResult();
                retval.user = new Contact(user, getContext());
                retval.image = new URL(ApiSection.SERVER_URL + img.getString("img"));
                return retval;
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (MalformedURLException | ParseException | JSONException e) {
            Log.d("[TP-diploma]", e.getMessage());
            throw new IOException("Server error", e);
        }
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
