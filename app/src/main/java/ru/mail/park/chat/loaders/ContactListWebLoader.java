package ru.mail.park.chat.loaders;

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
public class ContactListWebLoader extends ContactListDBLoader {
    private boolean activated = true;

    public ContactListWebLoader(@NonNull Context context, int id) {
        super(context, id);
    }

    @Override
    public @Nullable List<Contact> loadInBackground() {
        Contacts contactsAPI = new Contacts(getContext());
        List<Contact> contactList = null;
        try {
            Pair<List<Contact>, Integer> result = contactsAPI.getContacts(activated);
            contactList = result.first;
            ContactHelper contactHelper = new ContactHelper(getContext());
            contactHelper.updateContactsList(contactList);
            Log.v(getClass().getName(), String.valueOf(result.first.size()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contactList;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}
