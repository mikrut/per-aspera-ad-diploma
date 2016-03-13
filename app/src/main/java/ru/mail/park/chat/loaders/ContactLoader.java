package ru.mail.park.chat.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import ru.mail.park.chat.database.ContactHelper;
import ru.mail.park.chat.models.Contact;

public class ContactLoader extends AsyncTaskLoader<List<Contact>> {
    private List<Contact> contacts;

    public ContactLoader(@NonNull Context context) {
        super(context);

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
}