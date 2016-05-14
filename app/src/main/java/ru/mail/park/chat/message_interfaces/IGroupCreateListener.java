package ru.mail.park.chat.message_interfaces;

import ru.mail.park.chat.models.Chat;

/**
 * Created by Михаил on 14.05.2016.
 */
public interface IGroupCreateListener {
    void onChatCreated(Chat chat);
}
