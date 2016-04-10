package ru.mail.park.chat.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import ru.mail.park.chat.api.Chats;
import ru.mail.park.chat.database.ChatHelper;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Message;

/**
 * Created by mikrut on 10.04.16.
 */
public class MessagesLoader extends AsyncTaskLoader<List<Message>> {
    public static final String CID_ARG = MessagesLoader.class.getCanonicalName() + "CID_ARG";
    List<Message> messages;
    String chatID;

    MessagesLoader(@NonNull Context context, Bundle args) {
        super(context);
        chatID = args.getString(CID_ARG);
    }

    @Override
    public List<Message> loadInBackground() {
        Chats chats = new Chats(getContext());
        try {
            messages = chats.getMessages(chatID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return messages;
    }

        @Override
        protected void onStartLoading() {
        if (messages != null) {
            deliverResult(messages);
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
        messages = null;
    }
}
