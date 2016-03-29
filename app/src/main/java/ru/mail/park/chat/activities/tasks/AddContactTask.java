package ru.mail.park.chat.activities.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

import ru.mail.park.chat.activities.UserProfileActivity;
import ru.mail.park.chat.api.Contacts;

/**
 * Created by Михаил on 27.03.2016.
 */
public class AddContactTask extends AsyncTask<String, Void, Boolean> {
    private Context context;
    private String uid;

    public AddContactTask(Context context) {
        this.context = context;
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
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra(UserProfileActivity.UID_EXTRA, uid);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Failed to add contact", Toast.LENGTH_SHORT).show();
        }
    }
}
