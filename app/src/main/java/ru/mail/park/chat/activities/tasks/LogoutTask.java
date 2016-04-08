package ru.mail.park.chat.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import ru.mail.park.chat.activities.auth_logout.IAuthLogout;
import ru.mail.park.chat.api.Auth;

/**
 * Created by 1запуск BeCompact on 27.03.2016.
 */
public class LogoutTask extends AsyncTask<String, Void, Boolean> {
    private final IAuthLogout listener;
    private Auth auth;
    private final Context context;

    public LogoutTask(Context context, IAuthLogout listener) {
        this.context = context;
        this.listener = listener;
        if (listener != null) {
            listener.onStartLogout();
        }
    }

    @Override
    protected Boolean doInBackground(String... params) {
        auth = new Auth(context);
        Log.d("[TechMail]", "calling doInBackground");
        String token = params[0];

        try {
            auth.logOut(token);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        Log.d("[TechMail]", "calling onPostExecute");
        if (listener != null) {
            if (result) {
                listener.onLogoutSuccess();
            } else {
                listener.onLogoutFail();
            }
        }
    }
}
