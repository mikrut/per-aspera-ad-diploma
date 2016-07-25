package ru.mail.park.chat.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;

import ru.mail.park.chat.api.rest.Contacts;

/**
 * Created by Михаил on 21.05.2016.
 */
public class DeleteContactTask extends AsyncTask<String, Void, Boolean> {
    public interface DeleteContactCallbacks {
        void onDeleted(boolean success);
    }

    private final Context context;
    private final DeleteContactCallbacks callbacks;
    private String uid;

    public DeleteContactTask(Context context, DeleteContactCallbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        uid = params[0];
        Contacts contacts = new Contacts(context);
        try {
            contacts.deleteContact(uid);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (callbacks != null) {
            callbacks.onDeleted(aBoolean);
        }
    }
}
