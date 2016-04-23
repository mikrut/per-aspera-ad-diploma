package ru.mail.park.chat.loaders;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.List;

import ru.mail.park.chat.activities.DialogActivity;
import ru.mail.park.chat.database.MessagesHelper;
import ru.mail.park.chat.models.Message;

/**
 * Created by mikrut on 22.04.16.
 */
public class MessagesDBLoader extends MessagesLoader {
    public MessagesDBLoader(@NonNull Context context, Bundle args) {
        super(context, args);
    }

    @Override
    public List<Message> loadInBackground() {
        if (chatID != null) {
            MessagesHelper messagesHelper = new MessagesHelper(getContext());
            return messagesHelper.getMessages(chatID);
        } else if (userID != null) {
            // TODO: upload by uid from db
            return null;
        } else {
            return null;
        }
    }

    @Override
    public int getId() {
        return DialogActivity.MESSAGES_DB_LOADER;
    }
}
