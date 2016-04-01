package ru.mail.park.chat.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import ru.mail.park.chat.activities.auth_logout.IAuthLogout;
import ru.mail.park.chat.api.Auth;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by 1запуск BeCompact on 27.03.2016.
 */
public class LogoutTask  extends AsyncTask<String, Void, Void> {
    private IAuthLogout listener;
    private Auth auth;
    private Context context;

    public LogoutTask(Context context, IAuthLogout listener) {
        auth = new Auth(context);
        this.context = context;
        this.listener = listener;
        listener.onStartLogout();
    }

    @Override
    protected Void doInBackground(String... params) {
        Log.d("[TechMail]", "calling doInBackground");
        String token = params[0];

        OwnerProfile user = new OwnerProfile(context);
        String message = null;

        try {
            auth.logOut(token);


        } catch (IOException e) {
            message = e.getLocalizedMessage();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        Log.d("[TechMail]", "calling onPostExecute");
        listener.onLogoutSuccess();
    }
}
