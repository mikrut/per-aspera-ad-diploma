package ru.mail.park.chat.message_interfaces;

/**
 * Created by Михаил on 24.04.2016.
 */
public interface IMessageSender {
    void sendMessage(String chatID, String message);
    void sendFirstMessage(String userID, String message);
    void disconnect();
}
