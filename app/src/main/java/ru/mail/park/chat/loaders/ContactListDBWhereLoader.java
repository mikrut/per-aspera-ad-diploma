package ru.mail.park.chat.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.List;

import ru.mail.park.chat.database.ContactHelper;
import ru.mail.park.chat.models.Contact;

/**
 * Created by 1запуск BeCompact on 03.04.2016.
 */
public class ContactListDBWhereLoader extends AsyncTaskLoader<List<Contact>> {
    private List<Contact> contacts;
    private String searchQuery;
    private ContactHelper contactHelper;

    public ContactListDBWhereLoader(Context context) {
        super(context);
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    @Override
    @Nullable
    public List<Contact> loadInBackground() {
        contactHelper = new ContactHelper(getContext());
        Cursor contactsSearched = contactHelper.getParticularContactsCursor(searchQuery);

        for(contactsSearched.moveToFirst(); !contactsSearched.isAfterLast(); contactsSearched.moveToNext()) {
            contacts.add(new Contact(contactsSearched));
        }

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
