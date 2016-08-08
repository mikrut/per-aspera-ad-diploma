package ru.mail.park.chat.activities.interfaces;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.mail.park.chat.models.Contact;

/**
 * Created by mikrut on 08.08.16.
 */
public interface ILoadOnionListener {
    void onOnionLoaded(@NonNull Contact forContact, @Nullable String withResult);
}
