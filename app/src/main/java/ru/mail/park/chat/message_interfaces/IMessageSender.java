package ru.mail.park.chat.message_interfaces;

import java.util.List;

import ru.mail.park.chat.models.AttachedFile;

/**
 * Created by Михаил on 24.04.2016.
 */
public interface IMessageSender {
    void sendMessage(String chatID, String message);
    void sendMessage(String chatID, String message, List<AttachedFile> files);
    void sendFirstMessage(String userID, String message);
    void sendFirstMessage(String userID, String message, List<AttachedFile> files);
    void disconnect();
}
