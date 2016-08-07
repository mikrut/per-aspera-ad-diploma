package ru.mail.park.chat.api.rest;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.api.MultipartProfileUpdater;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by mikrut on 22.03.16
 */

public class Users extends ApiSection {
    private static final String URL_ADDITION = "users";

    @Override
    protected Uri getUrlAddition() {
        return super.getUrlAddition().buildUpon().appendPath(URL_ADDITION).build();
    }

    public Users(@NonNull Context context) {
        super(context);
    }

    @Nullable
    public Contact getFullUser(@NonNull String uid) throws IOException {
        final String requestURL = "getFullUser";
        final String requestMethod = "POST";

        List<Pair<String, String>> parameters = new ArrayList<>(2);
        parameters.add(new Pair<>("idUser", uid));

        Contact user = null;
        try {
            String j = executeRequest(requestURL, requestMethod, parameters);
            JSONObject result = new JSONObject(j);
            final int status = result.getInt("status");
            Log.d("[TP-diploma]", result.toString());
            if (status == 200) {
                JSONObject data = result.getJSONObject("data");
                Log.d("[TP-diploma]", "getFullUser result: " + data.toString());
                user = new Contact(data, getContext());
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException | ParseException e) {
            throw new IOException("Server error", e);
        }

        return user;
    }

    @Nullable
    public Contact getUser(@NonNull String uid) throws IOException {
        List<Contact> userList = getUsers(uid);
        if (userList.size() > 0) {
            return userList.get(0);
        }
        return null;
    }

    @NonNull
    private List<Contact> getUsers(@NonNull String... uids) throws IOException {
        final String requestURL = "getUsers";
        final String requestMethod = "POST";

        List<Pair<String, String>> parameters = new ArrayList<>(uids.length + 1);
        for (int i = 0; i < uids.length; i++) {
            parameters.add(new Pair<>(String.format("idUsers[%d]", i), uids[i]));
        }

        return contactListFromRequest(requestURL, requestMethod, parameters);
    }

    @NonNull
    public List<Contact> search(@NonNull String login) throws IOException {
        final String requestURL = "search";
        final String requestMethod = "POST";

        List<Pair<String, String>> parameters = new ArrayList<>(2);
        parameters.add(new Pair<>("login", login));

        return contactListFromRequest(requestURL, requestMethod, parameters);
    }

    @NonNull
    private List<Contact> contactListFromRequest(String requestURL,
                                                 String requestMethod,
                                                 List<Pair<String, String>> parameters) throws IOException {
        List<Contact> contactList = new ArrayList<>();
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            final int status = result.getInt("status");
            if (status == 200) {
                JSONArray users = result.getJSONArray("data");

                for (int i = 0; i < users.length(); i++) {
                    Contact user = new Contact(users.getJSONObject(i), getContext());
                    contactList.add(user);
                }
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException | ParseException e) {
            throw new IOException("Server error", e);
        }

        return contactList;
    }

    public void updateOnion(String onionAddress, byte[] publicKeyHash, int port) throws IOException {
        final String requestURL = "update";
        final String requestMethod = "POST";

        List<Pair<String, String>> parameters = new ArrayList<>(4);
        parameters.add(new Pair<String, String>("onionAddress",  onionAddress));
        parameters.add(new Pair<String, String>("pubKeyHash",    Contact.getPubkeyDigestString(publicKeyHash)));
        parameters.add(new Pair<String, String>("port",          String.valueOf(port)));

        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            final int status = result.getInt("status");
            if (status != 200) {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            throw new IOException("Server error", e);
        }
    }

    public boolean updateProfile(OwnerProfile profile) throws IOException {
        final String requestURL = "updateProfile";
        final String requestMethod = "POST";

        List<Pair<String, String>> parameters = new ArrayList<>(2);
        parameters.add(new Pair<>("login", profile.getLogin()));
        parameters.add(new Pair<>("email", profile.getEmail()));
        parameters.add(new Pair<>("phone", profile.getPhone()));
        parameters.add(new Pair<>("firstName", profile.getFirstName()));
        parameters.add(new Pair<>("lastName", profile.getLastName()));

        try {
            String response = executeRequest(requestURL, requestMethod, parameters);
            Log.d("[TP-diploma]", response);
            JSONObject result = new JSONObject(response);
            final int status = result.getInt("status");
            if (status == 200) {
                return true;
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            throw new IOException("Server error", e);
        }
    }

    public boolean updateProfileLikeAPro(OwnerProfile profile, String accessToken, MultipartProfileUpdater.IUploadListener listener) throws IOException {
        final String requestURL = "users/update";
        final String SERVER_URL = "http://p30480.lab1.stud.tech-mail.ru/";

        List<Pair<String, String>> parameters = new ArrayList<>();

        if(profile.getLogin() != null)
            parameters.add(new Pair<>("login", profile.getLogin()));

        if(profile.getEmail() != null)
            parameters.add(new Pair<>("email", profile.getEmail()));

        if(profile.getFirstName() != null)
            parameters.add(new Pair<>("firstName", profile.getFirstName()));

        if(profile.getLastName() != null)
            parameters.add(new Pair<>("lastName", profile.getLastName()));

        parameters.add(new Pair<>("accessToken", accessToken));

        if(profile.getImg() != null)
            parameters.add(new Pair<>("img", profile.getImg()));

        if(profile.getAbout() != null)
            parameters.add(new Pair<>("aboutMe", profile.getAbout()));

        for(int i = 0; i < parameters.size(); i++) {
            Pair<String, String> p = parameters.get(i);
            Log.d("[TP-diploma]", p.first + ": " + (p.second == null ? "null" : p.second) );
        }

        Log.d("[TP-diploma]", "Number of parameters: " + parameters.size());

        MultipartProfileUpdater mpu = new MultipartProfileUpdater(SERVER_URL + requestURL, parameters);
        return mpu.Send_Now(listener);
    }

}
