package ru.mail.park.chat.api.websocket;

import ru.mail.park.chat.models.Chat;

/**
 * Created by Михаил on 14.05.2016.
 */
public interface IGroupCreateListener {
    void onChatCreated(Chat chat);
}
