package ru.mail.park.chat.loaders;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import ru.mail.park.chat.api.Chats;
import ru.mail.park.chat.models.Chat;

/**
 * Created by mikrut on 28.03.16.
 */
public class ChatWebLoader extends ChatLoader {
    public ChatWebLoader(@NonNull Context context) {
        super(context);
    }

    @Override
    public List<Chat> loadInBackground() {
        Chats chatsAPI = new Chats(getContext());
        try {
            chats = chatsAPI.getChats();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chats;
    }
}
