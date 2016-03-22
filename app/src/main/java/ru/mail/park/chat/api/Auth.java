package ru.mail.park.chat.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class Auth extends ApiSection {
    private static final String URL_ADDITION = "auth/";

    @Override
    protected String getUrlAddition() {
        return super.getUrlAddition() + URL_ADDITION;
    }

    private String login;
    private String password;
    private String email;

    public Auth(Context context) {
        super(context);
    }

    @NonNull
    public OwnerProfile signUp(String login, String password, String email) throws IOException {
        final String requestURL = "signUp";
        final String requestMethod = "PUT";

        List<Pair<String, String>> parameters = new ArrayList<>(4);
        parameters.add(new Pair<>("login", login));
        parameters.add(new Pair<>("password", password));
        parameters.add(new Pair<>("email", email));

        OwnerProfile user;
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            final int status = result.getInt("status");
            if(status == 200) {
                JSONObject data = result.getJSONObject("data");
                user = new OwnerProfile(data);
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException | ParseException e) {
            throw new IOException("Server error");
        }

        return user;
    }

    @NonNull
    public OwnerProfile signIn(String login, String password) throws IOException {
        final String requestURL = "signIn";
        final String requestMethod = "POST";

        List<Pair<String, String>> parameters = new ArrayList<>(3);
        parameters.add(new Pair<>("login", login));
        parameters.add(new Pair<>("password", password));

        OwnerProfile user;
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            final int status = result.getInt("status");
            if(status == 200) {
                JSONObject data = result.getJSONObject("data");
                user = new OwnerProfile(data);
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            throw new IOException("Server error");
        }

        return user;
    }

    public void logOut() throws IOException {
        final String requestURL = "logOut";
        final String requestMethod = "POST";

        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod));
            final int status = result.getInt("status");
            if(status != 200) {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            throw new IOException("Server error");
        }
    }

/*    public void showActiveSessions() {

    }

    public void closeSession() {

    }*/

    public boolean isLogged() {
        return getAuthToken() != null;
    }
}
