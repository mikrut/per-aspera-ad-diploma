package ru.mail.park.chat.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;

/**
 * Created by Михаил on 08.03.2016.
 */

// TODO: implement firstname, lastname + stringification
public class Contact implements Comparable<Contact> {
    private @NonNull String login;
    private @NonNull String id;
    private @Nullable String phone;
    private @Nullable Calendar lastSeen;

    @Deprecated
    public Contact() {
        login = "v.pupkin";
        id = "abcd1234";
        phone = "8-800-555-35-35";
        lastSeen = Calendar.getInstance();
    }

    public Contact(JSONObject contact) throws JSONException, ParseException {
        setId(contact.getString("id"));
        setLogin(contact.getString("login"));
        setPhone(contact.getString("phone"));

        DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        java.util.Date dateLastSeen = iso8601.parse(contact.getString("last_seen"));
        GregorianCalendar lastSeen = new GregorianCalendar();
        lastSeen.setTime(dateLastSeen);
        setLastSeen(lastSeen);
    }

    @NonNull
    public String getLogin() {
        return login;
    }

    public void setLogin(@NonNull String login) {
        this.login = login;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @Nullable
    public String getPhone() {
        return phone;
    }

    public void setPhone(@Nullable String phone) {
        this.phone = phone;
    }

    @Nullable
    public Calendar getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(@Nullable Calendar lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public int compareTo(Contact another) {
        return this.login.compareTo(another.getLogin());
    }
}
