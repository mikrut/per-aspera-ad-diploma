package ru.mail.park.chat.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

import ru.mail.park.chat.database.ContactHelper;
import ru.mail.park.chat.models.Contact;

public class ContactListDBLoader extends AsyncTaskLoader<List<Contact>> {
    private List<Contact> contacts;
    private final int id;

    public ContactListDBLoader(@NonNull Context context, int id) {
        super(context);
        this.id = id;
    }

    @Override
    @NonNull
    public List<Contact> loadInBackground() {
        ContactHelper contactHelper = new ContactHelper(getContext());
        contacts = contactHelper.getContactsList();
        return contacts;
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

    @Override
    public int getId() {
        return id;
    }
}