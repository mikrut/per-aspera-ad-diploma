package ru.mail.park.chat.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ru.mail.park.chat.activities.DialogActivity;
import ru.mail.park.chat.api.rest.Chats;
import ru.mail.park.chat.database.MessagesHelper;
import ru.mail.park.chat.helpers.DialogEndlessPagination;
import ru.mail.park.chat.models.Message;

/**
 * Created by mikrut on 10.04.16.
 */
public class MessagesLoader extends AsyncTaskLoader<List<Message>> {
    public static final String ARG_INDEX = DialogEndlessPagination.ARG_INDEX;
    public static final String CID_ARG = MessagesLoader.class.getCanonicalName() + ".CID_ARG";
    public static final String UID_ARG = MessagesLoader.class.getCanonicalName() + ".UID_ARG";

    List<Message> messages;
    String chatID;
    String userID;
    String index;

    public MessagesLoader(@NonNull Context context, Bundle args) {
        super(context);
        chatID = args.getString(CID_ARG);
        userID = args.getString(UID_ARG);
        index = args.getString(ARG_INDEX);
    }

    @Override
    public List<Message> loadInBackground() {
        if (chatID != null) {
            Chats chats = new Chats(getContext());
            try {
                Log.d("[TP-diploma]", "trying getMessages");
                messages = chats.getMessages(chatID, index);
                MessagesHelper messagesHelper = new MessagesHelper(getContext());
                if(messages == null) {
                    messages = messagesHelper.getMessages(chatID);
                } else {
                    messagesHelper.updateMessageList(messages, chatID);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            messages = new ArrayList<>();
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
