package ru.mail.park.chat.activities.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

import ru.mail.park.chat.activities.ProfileViewActivity;
import ru.mail.park.chat.api.rest.Contacts;

/**
 * Created by Михаил on 27.03.2016.
 */
public class AddContactTask extends AsyncTask<String, Void, Boolean> {
    private final Context context;
    private String uid;
    private IAddContactListener listener;

    public interface IAddContactListener {
        void OnAddContact(String uid);
        void OnAddContactFailed(String uid);
    }

    public AddContactTask(Context context) {
        this.context = context;
    }

    public void setListener(IAddContactListener listener) {
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        uid = params[0];
        Contacts contacts = new Contacts(context);
        try {
            return contacts.addContact(uid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean) {
            Toast.makeText(context, "Contact added. Wait for his ACK", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(context, ProfileViewActivity.class);
            intent.putExtra(ProfileViewActivity.UID_EXTRA, uid);
            context.startActivity(intent);

            if (listener != null) {
                listener.OnAddContact(uid);
            }
        } else {
            Toast.makeText(context, "Failed to add contact", Toast.LENGTH_SHORT).show();

            if (listener != null) {
                listener.OnAddContactFailed(uid);
            }
        }
    }
}
