package ru.mail.park.chat.activities.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.app.AlertDialog;
import android.util.Log;

import java.io.IOException;

import ru.mail.park.chat.api.MultipartProfileUpdater;
import ru.mail.park.chat.api.rest.Users;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by mikrut on 24.03.16.
 */
public class UpdateProfileTask extends AsyncTask<OwnerProfile, String, Boolean> {
    private final AlertDialog alertDialog;
    private final Activity activity;

    public UpdateProfileTask(AlertDialog dialog, Activity starterActivity) {
        alertDialog = dialog;
        this.activity = starterActivity;
    }

    @Override
    protected Boolean doInBackground(OwnerProfile... params) {
        OwnerProfile profile = params[0];
        if (profile != null) {
            publishProgress("Sending data to server");
            Users usersAPI = new Users(alertDialog.getContext());
            try {
                boolean success = usersAPI.updateProfileLikeAPro(profile, profile.getAuthToken(), (MultipartProfileUpdater.IUploadListener)activity);//usersAPI.updateProfile(profile);
                publishProgress("Saving to local db...");
                Log.d("[TP-diploma]", "updateProfileLikeAPro result: " + String.valueOf(success));
                if (success) {
                    Contact thisUser = usersAPI.getFullUser(profile.getUid());
                    profile = new OwnerProfile(thisUser, alertDialog.getContext());
                    profile.saveToPreferences(alertDialog.getContext());
                }
                return success;
            } catch (IOException e) {
                Log.d("[TP-diploma]", "Ebal ya etot exception: " + e.getMessage());
                publishProgress(e.getLocalizedMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (aBoolean) {
            alertDialog.setMessage("Success!");
            activity.finish();
        } else {
            alertDialog.setMessage("Error sending to server");
        }
        alertDialog.setCancelable(true);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        alertDialog.setMessage(values[0]);
    }
}
