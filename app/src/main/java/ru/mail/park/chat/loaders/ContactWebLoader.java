package ru.mail.park.chat.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.List;

import ru.mail.park.chat.api.Contacts;
import ru.mail.park.chat.database.ContactHelper;
import ru.mail.park.chat.models.Contact;

/**
 * Created by mikrut on 22.03.16.
 */
public class ContactWebLoader extends AsyncTaskLoader<List<Contact>> {
    private List<Contact> contacts;
    private int id;


    public ContactWebLoader(@NonNull Context context, int id) {
        super(context);
        this.id = id;
    }

    @Override
    public @Nullable List<Contact> loadInBackground() {
        Log.v("webloader", "load in back");
        Contacts contactsAPI = new Contacts(getContext());
        List<Contact> contactList = null;
        try {
            Pair<List<Contact>, Integer> result = contactsAPI.getContacts();
            contactList = result.first;
            ContactHelper contactHelper = new ContactHelper(getContext());
            contactHelper.updateContactsList(contactList);
            Log.v(getClass().getName(), String.valueOf(result.first.size()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contactList;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    protected void onStartLoading() {
        if (contacts != null) {
            deliverResult(contacts);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        contacts = null;
    }
}
