package ru.mail.park.chat.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import java.io.IOException;

import ru.mail.park.chat.api.Auth;
import ru.mail.park.chat.auth_signup.IAuthCallbacks;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by Михаил on 19.03.2016.
 */
public class LoginTask extends AsyncTask<String, Void, Pair<String, OwnerProfile>> {
    private final IAuthCallbacks listener;
    private final Auth auth;

    public LoginTask(Context context, IAuthCallbacks listener) {
        auth = new Auth(context);
        this.listener = listener;
        listener.onStartAuth();
    }

    @Override
    protected Pair<String, OwnerProfile> doInBackground(String... params) {
        String login = params[0];
        String password = params[1];

        OwnerProfile user = null;
        String message = null;

        try {
            user = auth.signIn(login, password);
        } catch (IOException e) {
            message = e.getLocalizedMessage();
        }

        return new Pair<>(message, user);
    }

    @Override
    protected void onPostExecute(Pair<String, OwnerProfile> result) {
        if (result.second != null) {
            listener.onLoginSuccess(result.second);
        } else {
            listener.onLoginFail(result.first);
        }
    }
}
