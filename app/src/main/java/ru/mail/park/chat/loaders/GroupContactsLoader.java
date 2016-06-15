package ru.mail.park.chat.loaders;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import ru.mail.park.chat.database.ContactsHelper;
import ru.mail.park.chat.database.ContactsToChatsHelper;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 13.06.2016.
 */
public class GroupContactsLoader extends ContactListDBLoader {
    private String cid;

    public GroupContactsLoader(@NonNull Context context, int id, @NonNull  String cid) {
        super(context, id);
        this.cid = cid;
    }

    @Override
    @NonNull
    public List<Contact> loadInBackground() {
        ContactsToChatsHelper helper = new ContactsToChatsHelper(getContext());
        contacts = helper.getContacts(cid);
        return contacts;
    }
}
