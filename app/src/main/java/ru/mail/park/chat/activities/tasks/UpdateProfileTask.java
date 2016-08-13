package ru.mail.park.chat.activities.tasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.app.AlertDialog;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

import ru.mail.park.chat.api.ApiSection;
import ru.mail.park.chat.activities.interfaces.IUploadListener;
import ru.mail.park.chat.api.rest.Users;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.loaders.images.ImageFetchTask;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by mikrut on 24.03.16.
 */
public class UpdateProfileTask extends AsyncTask<OwnerProfile, String, Boolean> {
    private static final String TAG = UpdateProfileTask.class.getSimpleName();

    private final AlertDialog alertDialog;
    private final ImageDownloadManager imageDownloadManager;
    private final Activity activity;

    public UpdateProfileTask(AlertDialog dialog, ImageDownloadManager imageDownloadManager,
                             Activity starterActivity) {
        alertDialog = dialog;
        this.imageDownloadManager = imageDownloadManager;
        this.activity = starterActivity;
    }

    @Override
    protected Boolean doInBackground(OwnerProfile... params) {
        OwnerProfile profile = params[0];
        String newImgPath = profile.getImg();

        publishProgress("Sending data to server");
        Users usersAPI = new Users(alertDialog.getContext());
        try {
            boolean success = usersAPI.updateProfile(profile);
            publishProgress("Saving to local db...");
            Log.d(TAG + ".result", String.valueOf(success));

            if (success) {
                Contact thisUser = usersAPI.getFullUser(profile.getUid());
                if (thisUser != null) {
                    profile = new OwnerProfile(thisUser, alertDialog.getContext());
                    profile.saveToPreferences(alertDialog.getContext());

                    if (imageDownloadManager != null && newImgPath != null) {
                        publishProgress("Updating image");

                        try {
                            URL imageURL = new URL(ApiSection.SERVER_URL + profile.getImg());

                            BitmapFactory.Options options = new BitmapFactory.Options();
                            File file = new File(newImgPath);
                            FileInputStream inputStream = new FileInputStream(file);
                            options.inPreferredConfig = Bitmap.Config.RGB_565;
                            Bitmap bm = BitmapFactory.decodeStream(inputStream, null, options);

                            ImageFetchTask.runConversions(imageDownloadManager, imageURL, ImageDownloadManager.Size.SCREEN_SIZE, bm);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            return success;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            publishProgress(e.getLocalizedMessage());
            return false;
        }
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

        if (activity != null) {
            ((IUploadListener) activity).onUploadComplete(String.valueOf(aBoolean));
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        alertDialog.setMessage(values[0]);
    }
}
