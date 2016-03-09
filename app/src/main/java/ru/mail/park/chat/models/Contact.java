package ru.mail.park.chat.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.mail.park.chat.database.ContactsContract;
public class Contact {
    @NonNull String cid;
    @NonNull String login;
    @Nullable String email;

    public Contact(Cursor cursor) {
        cid = cursor.getString(ContactsContract.PROJECTION_CID_INDEX);
        login = cursor.getString(ContactsContract.PROJECTION_LOGIN_INDEX);
        email = cursor.getString(ContactsContract.PROJECTION_EMAIL_INDEX);
    }

    @NonNull
    public String getCid() {
        return cid;
    }

    public void setCid(@NonNull String cid) {
        this.cid = cid;
    }

    @NonNull
    public String getLogin() {
        return login;
    }

    public void setLogin(@NonNull String login) {
        this.login = login;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nullable String email) {
        this.email = email;
    }

    @NonNull
    public ContentValues getContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ContactsContract.ContactsEntry.COLUMN_NAME_CID, cid);
        contentValues.put(ContactsContract.ContactsEntry.COLUMN_NAME_LOGIN, login);
        contentValues.put(ContactsContract.ContactsEntry.COLUMN_NAME_EMAIL, email);
        return contentValues;
    }
}
