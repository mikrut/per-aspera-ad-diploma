package ru.mail.park.chat.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.mail.park.chat.database.ContactsContract;
import ru.mail.park.chat.database.MessengerDBHelper;

/**
 * Created by Михаил on 08.03.2016.
 */

// TODO: implement drawables
public class Contact implements Comparable<Contact>, Serializable {
    private @NonNull String uid;
    private @NonNull String login;

    private @Nullable String email;
    private @Nullable String phone;
    private @Nullable String firstName;
    private @Nullable String lastName;
    private @Nullable String img;
    private @Nullable Calendar lastSeen;

    private boolean online = false;

    public enum Relation {FRIEND, SELF, OTHER}

    Contact(){}

    public Contact(JSONObject contact, Context context) throws JSONException, ParseException {
        Log.d("[TP-diploma]", contact.toString());
        String idParameterName = "";
        if (contact.has("id"))
            idParameterName = "id";
        if (contact.has("idUser"))
            idParameterName = "idUser";
        if (idParameterName.equals("")) {
            OwnerProfile owner = new OwnerProfile(context);
            uid = owner.getUid();
            login = owner.getLogin();
            firstName = owner.getFirstName();
            lastName = owner.getLastName();
            img = owner.getImg();
        } else {
            uid = contact.getString(idParameterName);
        }
        if (contact.has("login"))
            login = contact.getString("login");

        if (contact.has("email"))
            setEmail(contact.getString("email"));
        if (contact.has("phone"))
            setPhone(contact.getString("phone"));
        if (contact.has("firstName"))
            setFirstName(contact.getString("firstName"));
        if (contact.has("lastName"))
            setLastName(contact.getString("lastName"));

        if (contact.has("lastSeen")) {
            java.util.Date dateLastSeen = MessengerDBHelper.currentFormat.parse(contact.getString("lastSeen"));
            GregorianCalendar lastSeen = new GregorianCalendar();
            lastSeen.setTime(dateLastSeen);
            setLastSeen(lastSeen);
        }

        if (contact.has("online")) {
            setOnline(contact.getBoolean("online"));
        }

        if(contact.has("img")) {
            Log.d("[TP-diploma]", "has img");
            setImg(contact.getString("img"));
        }
    }

    public Contact(Cursor cursor) {
        uid = cursor.getString(ContactsContract.PROJECTION_UID_INDEX);
        login = cursor.getString(ContactsContract.PROJECTION_LOGIN_INDEX);

        email = cursor.getString(ContactsContract.PROJECTION_EMAIL_INDEX);
        phone = cursor.getString(ContactsContract.PROJECTION_PHONE_INDEX);
        firstName = cursor.getString(ContactsContract.PROJECTION_FIRST_NAME_INDEX);
        lastName = cursor.getString(ContactsContract.PROJECTION_LAST_NAME_INDEX);
    }

    public boolean isOnline() {
        return online;
    }

    private void setOnline(boolean online) {
        this.online = online;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    @NonNull
    public String getLogin() {
        return login;
    }

    public void setLogin(@NonNull String login) {
        this.login = login;
    }

    @Nullable
    public String getPhone() {
        return phone;
    }

    public void setPhone(@Nullable String phone) {
        if (TextUtils.equals(phone, ""))
            phone = null;
        this.phone = phone;
    }

    public @Nullable Calendar getLastSeen() {
        return lastSeen;
    }

    private void setLastSeen(@Nullable Calendar lastSeen) {
        this.lastSeen = lastSeen;
    }

    @Override
    public int compareTo(@NonNull Contact another) {
        return this.login.compareTo(another.getLogin());
    }
    

    public @Nullable String getEmail() {
        return email;
    }

    public void setEmail(@Nullable String email) {
        if (TextUtils.equals(email, ""))
            email = null;
        this.email = email;
    }

    @Nullable
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(@Nullable String firstName) {
        if (TextUtils.equals(firstName, ""))
            firstName = null;
        this.firstName = firstName;
    }

    @Nullable
    public String getLastName() {
        return lastName;
    }

    public void setLastName(@Nullable String lastName) {
        if (TextUtils.equals(lastName, ""))
            lastName = null;
        this.lastName = lastName;
    }

    public void setImg(String img) {
        Log.d("[TP-diploma]", "set img");
        this.img = img;
    }

    public String getImg() {
        return img;
    }

    @NonNull
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsContract.ContactsEntry.COLUMN_NAME_UID, uid);
        contentValues.put(ContactsContract.ContactsEntry.COLUMN_NAME_LOGIN, login);

        contentValues.put(ContactsContract.ContactsEntry.COLUMN_NAME_EMAIL, email);
        contentValues.put(ContactsContract.ContactsEntry.COLUMN_NAME_PHONE, phone);
        contentValues.put(ContactsContract.ContactsEntry.COLUMN_NAME_FIRST_NAME, firstName);
        contentValues.put(ContactsContract.ContactsEntry.COLUMN_NAME_LAST_NAME, lastName);
        return contentValues;
    }

    @NonNull
    public String getContactTitle() {
        StringBuilder titleBuilder = new StringBuilder();
        titleBuilder.append(firstName != null ? firstName : "");
        if (firstName != null && lastName != null)
            titleBuilder.append(" ");
        titleBuilder.append(lastName != null ? lastName : "");
        if (firstName == null && lastName == null) {
            return  getLogin();
        }
        return titleBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o != null && o instanceof Contact) {
            Contact other = (Contact) o;
            return TextUtils.equals(getLogin(), other.getLogin()) &&
                    TextUtils.equals(getEmail(), other.getEmail()) &&
                    TextUtils.equals(getPhone(), other.getPhone()) &&
                    TextUtils.equals(getFirstName(), other.getFirstName()) &&
                    TextUtils.equals(getLastName(), other.getLastName());
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getLogin())
                .append(getEmail())
                .append(getPhone())
                .append(getFirstName())
                .append(getLastName())
                .build();
    }
}
