package ru.mail.park.chat.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.List;

import ru.mail.park.chat.api.Contacts;
import ru.mail.park.chat.database.ContactsHelper;
import ru.mail.park.chat.models.Contact;

/**
 * Created by mikrut on 22.03.16.
 */
public class ContactListWebLoader extends ContactListDBLoader {
    private boolean activated = true;
    private boolean my = true;

    public ContactListWebLoader(@NonNull Context context, int id) {
        super(context, id);
    }

    @Override
    public @Nullable List<Contact> loadInBackground() {
        Contacts contactsAPI = new Contacts(getContext());
        contacts = null;
        try {
            Pair<List<Contact>, Integer> result = contactsAPI.getContacts(activated, my);
            contacts = result.first;
            if (activated) {
                ContactsHelper contactsHelper = new ContactsHelper(getContext());
                contactsHelper.updateContactsList(contacts);
            }
            Log.v(getClass().getSimpleName(), String.valueOf(result.first.size()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contacts;
    }

    public void setMy(boolean my) {
        this.my = my;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}
