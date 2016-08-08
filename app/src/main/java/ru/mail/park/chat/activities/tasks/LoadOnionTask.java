package ru.mail.park.chat.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;

import ru.mail.park.chat.activities.interfaces.ILoadOnionListener;
import ru.mail.park.chat.api.rest.P2P;
import ru.mail.park.chat.database.ContactsHelper;
import ru.mail.park.chat.models.Contact;

/**
 * Created by mikrut on 08.08.16.
 */
public class LoadOnionTask extends AsyncTask<Contact, Void, String> {
    private Context context;
    private ILoadOnionListener listener;
    private Contact contact;

    public LoadOnionTask(Context context, ILoadOnionListener loadOnionListener) {
        listener = loadOnionListener;
        this.context = context;
    }

    @Override
    protected String doInBackground(Contact... contacts) {
        contact = contacts[0];
        final P2P p2p = new P2P(context);
        try {
            P2P.OnionData data = p2p.getOnionData(contact.getUid());

            contact.setOnionAddress(data.onionAddress);
            contact.setPubkeyDigest(data.publicKeyDigest);
            ContactsHelper helper = new ContactsHelper(context);
            helper.saveContact(contact);
            helper.close();

            return data.onionAddress;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if (s != null) {
            listener.onOnionLoaded(contact, s);
        }
    }
}
