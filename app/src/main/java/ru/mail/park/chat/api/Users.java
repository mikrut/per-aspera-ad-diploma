package ru.mail.park.chat.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;

/**
 * Created by mikrut on 22.03.16
 */

public class Users extends ApiSection {
    private static final String URL_ADDITION = "users/";

    @Override
    protected String getUrlAddition() {
        return super.getUrlAddition() + URL_ADDITION;
    }

    public Users(@NonNull Context context) {
        super(context);
    }

    @Nullable
    public Contact getUser(@NonNull String uid) throws IOException {
        List<Contact> userlist = getUsers(uid);
        if (userlist.size() > 0) {
            return userlist.get(0);
        }
        return null;
    }

    @NonNull
    public List<Contact> getUsers(@NonNull String... uids) throws IOException {
        final String requestURL = "getUsers";
        final String requestMethod = "GET";

        List<Pair<String, String>> parameters = new ArrayList<>(uids.length + 1);
        for (int i = 0; i < uids.length; i++) {
            parameters.add(new Pair<>(String.format("idUsers[%d]", i), uids[i]));
        }

        List<Contact> contactList = new ArrayList<>(uids.length);
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            final int status = result.getInt("status");
            if (status == 200) {
                JSONArray users = result.getJSONArray("data");

                for (int i = 0; i < users.length(); i++) {
                    Contact user = new Contact(users.getJSONObject(i));
                    contactList.add(user);
                }
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException | ParseException e) {
            throw new IOException("Server error");
        }

        return contactList;
    }

}
