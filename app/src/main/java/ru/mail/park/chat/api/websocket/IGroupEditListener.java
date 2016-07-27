package ru.mail.park.chat.api.websocket;

import android.support.annotation.NonNull;

import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 27.06.2016.
 */
public interface IGroupEditListener {
    void onAddUser(@NonNull String cid, @NonNull  Contact user);
    void onUpdateName(@NonNull  String cid, @NonNull  String chatName);
}
