package ru.mail.park.chat.activities.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

import ru.mail.park.chat.activities.ProfileViewActivity;
import ru.mail.park.chat.api.Contacts;
import ru.mail.park.chat.database.ContactsHelper;
import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 14.05.2016.
 */
public class ActivateContactTask extends AsyncTask<Contact, Void, String> {
    private final Context context;

    public ActivateContactTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(Contact... params) {
        final Contact contact = params[0];
        final String uid = contact.getUid();
        Contacts contacts = new Contacts(context);
        try {
            contacts.activateContact(uid);
            ContactsHelper contactsHelper = new ContactsHelper(context);
            contactsHelper.saveContact(contact);
            return uid;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String uid) {
        if (uid != null) {
            Intent intent = new Intent(context, ProfileViewActivity.class);
            intent.putExtra(ProfileViewActivity.UID_EXTRA, uid);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Failed to activate contact", Toast.LENGTH_SHORT).show();
        }
    }
}
