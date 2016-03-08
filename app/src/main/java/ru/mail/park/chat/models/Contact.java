package ru.mail.park.chat.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;

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
