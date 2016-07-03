package ru.mail.park.chat.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import ru.mail.park.chat.activities.DialogActivity;
import ru.mail.park.chat.api.Chats;
import ru.mail.park.chat.database.MessagesHelper;
import ru.mail.park.chat.helpers.ScrollEndlessPagination;
import ru.mail.park.chat.models.Message;

/**
 * Created by mikrut on 10.04.16.
 */
public class MessagesLoader extends AsyncTaskLoader<List<Message>> {
    public static final String ARG_PAGE = ScrollEndlessPagination.ARG_PAGE;
    public static final String CID_ARG = MessagesLoader.class.getCanonicalName() + ".CID_ARG";
    public static final String UID_ARG = MessagesLoader.class.getCanonicalName() + ".UID_ARG";

    List<Message> messages;
    String chatID;
    String userID;
    int page = 1;

    public MessagesLoader(@NonNull Context context, Bundle args) {
        super(context);
        chatID = args.getString(CID_ARG);
        userID = args.getString(UID_ARG);
        page = args.getInt(ARG_PAGE, 1);
    }

    @Override
    public List<Message> loadInBackground() {
        Chats chats = new Chats(getContext());
        try {
            Log.d("[TP-diploma]", "trying getMessages");
            messages = chats.getMessages(chatID, page);
            MessagesHelper messagesHelper = new MessagesHelper(getContext());
            if(messages == null) {
                messages = messagesHelper.getMessages(chatID);
            } else {
                messagesHelper.updateMessageList(messages, chatID);
            }
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

    protected String getChatID() {
        return chatID;
    }

    @Override
    public int getId() {
        return DialogActivity.MESSAGES_WEB_LOADER;
    }
}
