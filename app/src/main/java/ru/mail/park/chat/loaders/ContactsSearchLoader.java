package ru.mail.park.chat.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.List;

import ru.mail.park.chat.api.rest.Users;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 27.03.2016.
 */
public class ContactsSearchLoader extends AsyncTaskLoader<List<Contact>> {
    private List<Contact> contacts;
    private String searchQuery;

    public ContactsSearchLoader(Context context) {
        super(context);
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    @Override
    @Nullable
    public List<Contact> loadInBackground() {
        Users users = new Users(getContext());
        try {
            return users.search(searchQuery);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
