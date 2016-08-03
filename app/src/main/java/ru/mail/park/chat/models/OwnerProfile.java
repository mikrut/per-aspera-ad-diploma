package ru.mail.park.chat.models;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import ru.mail.park.chat.activities.LoginActivity;
import ru.mail.park.chat.activities.auth_logout.IAuthLogout;
import ru.mail.park.chat.activities.tasks.LogoutTask;
import ru.mail.park.chat.database.MessengerDBHelper;
import ru.mail.park.chat.database.PreferenceConstants;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;

/**
 * Created by Михаил on 19.03.2016.
 */
public class OwnerProfile extends Contact {
    @Nullable
    private String authToken;

    private OwnerProfile(Cursor cursor){}

    public OwnerProfile(JSONObject owner) throws JSONException, ParseException {
        super(owner, null);
        setAuthToken(owner.getString("accessToken"));
    }

    public OwnerProfile(Contact contact, Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PreferenceConstants.PREFERENCE_NAME,
                        Context.MODE_PRIVATE);

        setEmail(contact.getEmail());
        setPhone(contact.getPhone());
        setFirstName(contact.getFirstName());
        setLastName(contact.getLastName());
        setLogin(contact.getLogin());
        setUid(contact.getUid());
        setImg(contact.getImg());
        setAbout(contact.getAbout());

        setAuthToken(sharedPreferences.getString(PreferenceConstants.AUTH_TOKEN_N, null));
    }

    public OwnerProfile() {
        super();
    }

    public OwnerProfile(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PreferenceConstants.PREFERENCE_NAME,
                        Context.MODE_PRIVATE);

        setEmail(sharedPreferences.getString(PreferenceConstants.USER_EMAIL_N, null));
        setLogin(sharedPreferences.getString(PreferenceConstants.USER_LOGIN_N, null));
        setUid(sharedPreferences.getString(PreferenceConstants.USER_UID_N, null));
        setPhone(sharedPreferences.getString(PreferenceConstants.USER_PHONE_N, null));
        setFirstName(sharedPreferences.getString(PreferenceConstants.USER_FIRST_NAME_N, null));
        setLastName(sharedPreferences.getString(PreferenceConstants.USER_LAST_NAME_N, null));
        setAuthToken(sharedPreferences.getString(PreferenceConstants.AUTH_TOKEN_N, null));
        setImg(sharedPreferences.getString(PreferenceConstants.USER_IMG_N, null));
        setAbout(sharedPreferences.getString(PreferenceConstants.USER_ABOUT_N, null));
    }



    public void saveToPreferences(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PreferenceConstants.PREFERENCE_NAME,
                        Context.MODE_APPEND);
        SharedPreferences.Editor preferenceEditor = sharedPreferences.edit();

        preferenceEditor.putString(PreferenceConstants.USER_EMAIL_N, getEmail());
        preferenceEditor.putString(PreferenceConstants.USER_LOGIN_N, getLogin());
        preferenceEditor.putString(PreferenceConstants.USER_PHONE_N, getPhone());
        preferenceEditor.putString(PreferenceConstants.USER_UID_N, getUid());
        preferenceEditor.putString(PreferenceConstants.USER_FIRST_NAME_N, getFirstName());
        preferenceEditor.putString(PreferenceConstants.USER_LAST_NAME_N, getLastName());
        preferenceEditor.putString(PreferenceConstants.AUTH_TOKEN_N, getAuthToken());
        preferenceEditor.putString(PreferenceConstants.USER_IMG_N, getImg());
        preferenceEditor.putString(PreferenceConstants.USER_ABOUT_N, getAbout());

        preferenceEditor.apply();
    }

    public void removeFromPreferences(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PreferenceConstants.PREFERENCE_NAME,
                        Context.MODE_APPEND);
        SharedPreferences.Editor preferenceEditor = sharedPreferences.edit();
        preferenceEditor.clear();
        preferenceEditor.apply();
    }

    @Nullable
    public String getAuthToken() {
        return authToken;
    }

    private void setAuthToken(@Nullable String authToken) {
        this.authToken = authToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o != null && o instanceof OwnerProfile) {
            OwnerProfile other = (OwnerProfile) o;
            return TextUtils.equals(getLogin(), other.getLogin()) &&
                    TextUtils.equals(getEmail(), other.getEmail()) &&
                    TextUtils.equals(getPhone(), other.getPhone()) &&
                    TextUtils.equals(getFirstName(), other.getFirstName()) &&
                    TextUtils.equals(getLastName(), other.getLastName()) &&
                    TextUtils.equals(getImg(), other.getImg()) &&
                    TextUtils.equals(getAbout(), other.getAbout());
        }
        return super.equals(o);
    }

    public void logout(Context context, @Nullable ImageDownloadManager mgr) {
        new LogoutTask(context).execute(authToken);

        removeFromPreferences(context);
        MessengerDBHelper dbHelper = new MessengerDBHelper(context);
        dbHelper.clearDatabase();

        if (mgr != null) {
            mgr.clearDiskCache();
        }

        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
