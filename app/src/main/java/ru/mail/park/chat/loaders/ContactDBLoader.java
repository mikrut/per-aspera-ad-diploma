package ru.mail.park.chat.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import ru.mail.park.chat.database.ContactHelper;
import ru.mail.park.chat.models.Contact;

public class ContactDBLoader extends AsyncTaskLoader<List<Contact>> {
    private List<Contact> contacts;
    private int id;

    public ContactDBLoader(@NonNull Context context, int id) {
        super(context);
        this.id = id;
    }

    @Override
    public List<Contact> loadInBackground() {
        ContactHelper chatHelper = new ContactHelper(getContext());
        contacts = chatHelper.getContactsList();
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