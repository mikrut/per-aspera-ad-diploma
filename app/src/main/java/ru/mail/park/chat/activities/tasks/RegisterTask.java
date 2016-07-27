package ru.mail.park.chat.activities.tasks;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.commons.lang3.tuple.Triple;

import java.io.IOException;
import java.util.Map;

import ru.mail.park.chat.api.rest.Auth;
import ru.mail.park.chat.auth_signup.IRegisterCallbacks;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by Михаил on 27.03.2016.
 */
public class RegisterTask extends AsyncTask<String, Void, Triple<String, Map<IRegisterCallbacks.ErrorType, String>, OwnerProfile>> {
    private final IRegisterCallbacks listener;
    private final Auth auth;

    public RegisterTask(Context context, IRegisterCallbacks listener) {
        auth = new Auth(context);
        this.listener = listener;
        listener.onRegistrationStart();
    }

    @Override
    protected Triple<String, Map<IRegisterCallbacks.ErrorType, String>, OwnerProfile> doInBackground(String... params) {
        String login = params[0];
        String firstName = params[1];
        String lastName = params[2];
        String password = params[3];
        String email = params[4];
        String imgPath = params[5];

        OwnerProfile user = null;
        String message = null;
        Map<IRegisterCallbacks.ErrorType, String> errors = null;

        try {
            user = auth.signUp(login, firstName, lastName, password, email, imgPath);
        } catch (IOException e) {
            message = e.getLocalizedMessage();
        } catch (Auth.SignUpException e) {
            errors = e.getErrorTypeStringMap();
        }

        return Triple.of(message, errors, user);
    }

    @Override
    protected void onPostExecute(Triple<String, Map<IRegisterCallbacks.ErrorType, String>, OwnerProfile> result) {
        listener.onRegistrationFinish();

        if (result.getRight() != null) {
            listener.onRegistrationSuccess(result.getRight());
        } else if (result.getMiddle() != null) {
            listener.onRegistrationFail(result.getMiddle());
        } else {
            listener.onRegistrationFail(null);
        }
    }
}
