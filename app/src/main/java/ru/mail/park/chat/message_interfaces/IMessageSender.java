package ru.mail.park.chat.message_interfaces;

import android.support.annotation.NonNull;

import java.util.List;

import ru.mail.park.chat.models.AttachedFile;
import ru.mail.park.chat.models.Message;

/**
 * Created by Михаил on 24.04.2016.
 */
public interface IMessageSender {
    void sendMessage(String chatID, Message message);
    void sendFirstMessage(String userID, Message message);
    boolean isConnected();
    void disconnect();
    void reconnect();
    void write(@NonNull String cid);
}
