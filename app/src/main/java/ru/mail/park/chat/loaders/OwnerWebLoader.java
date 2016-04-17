package ru.mail.park.chat.loaders;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by Михаил on 08.04.2016.
 */
public class OwnerWebLoader extends ProfileWebLoader {
    public OwnerWebLoader(@NonNull Context context, int id, Bundle args) {
        super(context, id, args);
    }

    @Override
    public Contact loadInBackground() {
        Contact contact = super.loadInBackground();
        OwnerProfile ownerProfile = null;
        if (contact != null) {
            ownerProfile = new OwnerProfile(contact, getContext());
            ownerProfile.saveToPreferences(getContext());
        }
        return ownerProfile;
    }
}
