package ru.mail.park.chat.loaders;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import ru.mail.park.chat.activities.ChatsActivity;
import ru.mail.park.chat.database.ChatsHelper;
import ru.mail.park.chat.models.Chat;

/**
 * Created by Михаил on 15.04.2016.
 */
public class ChatSearchLoader extends ChatLoader {
    @NonNull String queryString;

    public ChatSearchLoader(@NonNull Context context) {
        super(context);
    }

    public void setQueryString(@NonNull String queryString) {
        this.queryString = queryString;
    }

    @Override
    public List<Chat> loadInBackground() {
        ChatsHelper chatsHelper = new ChatsHelper(getContext());
        chats = chatsHelper.getChatsList(queryString);
        chatsHelper.close();
        return chats;
    }

    @Override
    public int getId() {
        return ChatsActivity.CHAT_SEARCH_LOADER;
    }
}
