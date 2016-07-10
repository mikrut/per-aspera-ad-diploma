package ru.mail.park.chat.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.IOException;

import ru.mail.park.chat.api.rest.Users;
import ru.mail.park.chat.models.Contact;

/**
 * Created by mikrut on 22.03.16.
 */
public class ProfileWebLoader extends AsyncTaskLoader<Contact> {
    public static final String UID_ARG = "uid";

    private Contact profile;
    private final int id;
    private final String uid;

    public ProfileWebLoader(@NonNull Context context, int id, Bundle args) {
        super(context);
        this.id = id;
        this.uid = args.getString(UID_ARG);
    }


    @Override
    public Contact loadInBackground() {
        Users usersAPI = new Users(getContext());
        Contact user = null;
        try {
           user = usersAPI.getFullUser(uid);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    protected void onStartLoading() {
        if (profile != null) {
            deliverResult(profile);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        profile = null;
    }

    @Override
    public int getId() {
        return id;
    }

}
