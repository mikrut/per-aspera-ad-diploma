package ru.mail.park.chat.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import ru.mail.park.chat.activities.DialogActivity;
import ru.mail.park.chat.database.ChatsHelper;
import ru.mail.park.chat.models.Chat;

/**
 * Created by Михаил on 03.06.2016.
 */
public class ChatInfoLoader extends AsyncTaskLoader<Chat> {
    public static final String CID_ARG = ChatInfoLoader.class.getCanonicalName() + ".CID_ARG";
    Chat chat;
    String chatID;

    public ChatInfoLoader(@NonNull Context context, Bundle args) {
        super(context);
        chatID = args.getString(CID_ARG, null);
    }

    @Override
    public Chat loadInBackground() {
        if (chatID != null) {
            ChatsHelper helper = new ChatsHelper(getContext());
            Chat chat = helper.getChat(chatID);
            helper.close();
            return chat;
        }
        return null;
    }

    @Override
    protected void onStartLoading() {
        if (chat != null) {
            deliverResult(chat);
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
        chat = null;
    }

    @Override
    public int getId() {
        return DialogActivity.CHAT_DB_LOADER;
    }
}
