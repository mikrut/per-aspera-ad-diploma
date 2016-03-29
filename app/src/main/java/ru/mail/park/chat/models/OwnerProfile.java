package ru.mail.park.chat.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import ru.mail.park.chat.R;
import ru.mail.park.chat.database.PreferenceConstants;

/**
 * Created by Михаил on 19.03.2016.
 */
public class OwnerProfile extends Contact {
    @Nullable
    private String authToken;

    private OwnerProfile(Cursor cursor){}

    public OwnerProfile(JSONObject owner) throws JSONException, ParseException {
        super(owner);
        setAuthToken(owner.getString("accessToken"));
    }


    public OwnerProfile(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(PreferenceConstants.PREFERENCE_NAME,
                        Context.MODE_PRIVATE);

        setEmail(sharedPreferences.getString(PreferenceConstants.USER_EMAIL_N, null));
        setLogin(sharedPreferences.getString(PreferenceConstants.USER_LOGIN_N, null));
        setUid(sharedPreferences.getString(PreferenceConstants.USER_UID_N, null));
        setPhone(sharedPreferences.getString(PreferenceConstants.USER_PHONE_N, null));
        setAuthToken(sharedPreferences.getString(PreferenceConstants.AUTH_TOKEN_N, null));
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
        preferenceEditor.putString(PreferenceConstants.AUTH_TOKEN_N, getAuthToken());

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

    public void setAuthToken(@Nullable String authToken) {
        this.authToken = authToken;
    }
}
