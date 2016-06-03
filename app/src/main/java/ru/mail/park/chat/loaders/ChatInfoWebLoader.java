package ru.mail.park.chat.loaders;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.IOException;

import ru.mail.park.chat.activities.DialogActivity;
import ru.mail.park.chat.api.Chats;
import ru.mail.park.chat.models.Chat;

/**
 * Created by Михаил on 03.06.2016.
 */
public class ChatInfoWebLoader extends ChatInfoLoader {
    public ChatInfoWebLoader(@NonNull Context context, Bundle args) {
        super(context, args);
    }

    @Override
    public Chat loadInBackground() {
        if (chatID != null) {
            try {
                Chats chatsApi = new Chats(getContext());
                return chatsApi.getChatInfo(chatID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public int getId() {
        return DialogActivity.CHAT_WEB_LOADER;
    }
}
