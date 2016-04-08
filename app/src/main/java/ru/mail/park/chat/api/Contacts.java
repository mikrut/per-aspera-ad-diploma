package ru.mail.park.chat.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class Contacts extends ApiSection {
    private static final String URL_ADDITION = "contacts/";

    @Override
    protected String getUrlAddition() {
        return super.getUrlAddition() + URL_ADDITION;
    }

    public Contacts(@NonNull Context context) {
        super(context);
    }

    @NonNull
    public Pair<List<Contact>, Integer> getContacts() throws IOException {
        final String requestURL = "list";
        final String requestMethod = "POST";

        OwnerProfile ownerProfile = new OwnerProfile(getContext());
        List<Pair<String, String>> parameters = new ArrayList<>(2);
        parameters.add(new Pair<>("id", ownerProfile.getUid()));

        int contactsLength;
        List<Contact> contactList;
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            final int status = result.getInt("status");
            if(status == 200) {
                // JSONObject data = result.getJSONObject("data");
                // contactsLength = data.getInt("contacts_length");
                JSONArray contacts = result.getJSONArray("data");
                contactsLength = contacts.length();

                contactList = contactsFrom(contacts);
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            throw new IOException("Server error");
        }

        return new Pair<>(contactList, contactsLength);
    }

    public boolean addContact(@NonNull String uid) throws IOException {
        final String requestURL = "info";
        final String requestMethod = "POST";

        List<Pair<String, String>> parameters = new ArrayList<>(2);
        parameters.add(new Pair<>("idUser", uid));

        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            final int status = result.getInt("status");
            if(status == 200) {
                return true;
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            throw new IOException("Server error");
        }
    }

    public int deleteContact(@NonNull String uid) throws IOException {
        final String requestURL = "info";
        final String requestMethod = "DELETE";

        List<Pair<String, String>> parameters = new ArrayList<>(2);
        parameters.add(new Pair<>("uid", uid));

        int contactsLength;
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            final int status = result.getInt("status");
            if(status == 200) {
                JSONObject data = result.getJSONObject("data");
                contactsLength = data.getInt("contacts_length");
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            throw new IOException("Server error");
        }

        return contactsLength;
    }

    public static class SearchResult {
        public int totalLength;
        public int respondedLength;
        public int minIndex;
        public List<Contact> contacts;

        public SearchResult(JSONObject data) throws JSONException, ParseException {
            totalLength = data.getInt("total_length");
            respondedLength = data.getInt("responded_length");
            minIndex = data.getInt("min_index");
            contacts = contactsFrom(data.getJSONArray("contacts"));
        }
    }

    @NonNull
    public SearchResult searchUser(@NonNull String partOfLogin) throws IOException, ParseException {
        return searchUser(partOfLogin, 0, 0);
    }

    @NonNull
    private SearchResult searchUser(@NonNull String partOfLogin, int minIndex, int responseLength) throws IOException, ParseException {
        final String requestURL = "info";
        final String requestMethod = "GET";


        List<Pair<String, String>> parameters = new ArrayList<>(4);
        parameters.add(new Pair<>("login", partOfLogin));
        parameters.add(new Pair<>("min_index", String.valueOf(minIndex)));
        parameters.add(new Pair<>("response_length", String.valueOf(responseLength)));

        SearchResult searchResult;
        try {
            JSONObject result = new JSONObject(executeRequest(requestURL, requestMethod, parameters));
            final int status = result.getInt("status");
            if(status == 200) {
                JSONObject data = result.getJSONObject("data");
                searchResult = new SearchResult(data);
            } else {
                String message = result.getString("message");
                throw new IOException(message);
            }
        } catch (JSONException e) {
            throw new IOException("Server error");
        }

        return searchResult;
    }


    private static List<Contact> contactsFrom(JSONArray contacts) throws JSONException, ParseException {
        List<Contact> contactList = new ArrayList<>(contacts.length());
        for (int i = 0; i < contacts.length(); i++) {
            Contact contact = new Contact(contacts.getJSONObject(i));
            contactList.add(contact);
        }
        return contactList;
    }
}
