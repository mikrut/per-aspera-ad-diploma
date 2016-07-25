package ru.mail.park.chat.loaders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import ru.mail.park.chat.api.rest.Chats;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;

/**
 * Created by mikrut on 05.07.16.
 */
public class GroupContactsWebLoader extends GroupContactsLoader {
    public GroupContactsWebLoader(@NonNull Context context, int id, @NonNull String cid) {
        super(context, id, cid);
    }

    @Override
    @NonNull
    public List<Contact> loadInBackground() {
        Chats chats = new Chats(getContext());
        try {
            Chat chat = chats.getChatInfo(getCid());
            return chat.getChatUsers();
        } catch (IOException e) {
            Log.w(GroupContactsWebLoader.class.getSimpleName(), e.getLocalizedMessage());
        }
        return null;
    }
}
