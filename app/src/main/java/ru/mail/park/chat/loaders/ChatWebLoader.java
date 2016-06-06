package ru.mail.park.chat.loaders;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import ru.mail.park.chat.activities.ChatsActivity;
import ru.mail.park.chat.api.Chats;
import ru.mail.park.chat.helpers.ScrollEnlessPagination;
import ru.mail.park.chat.models.Chat;

/**
 * Created by mikrut on 28.03.16.
 */
public class ChatWebLoader extends ChatLoader {
    public static final String ARG_PAGE = ScrollEnlessPagination.ARG_PAGE;
    int page = 1;

    public ChatWebLoader(@NonNull Context context) {
        this(context, null);
    }

    public ChatWebLoader(@NonNull Context context, Bundle args) {
        super(context);
        if (args != null) {
            page = args.getInt(ARG_PAGE);
        }
    }

    @Override
    public List<Chat> loadInBackground() {
        Chats chatsAPI = new Chats(getContext());
        try {
            chats = chatsAPI.getChats(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chats;
    }

    @Override
    public int getId() {
        return ChatsActivity.CHAT_WEB_LOADER;
    }
}
