package ru.mail.park.chat.api.websocket;

import com.neovisionaries.ws.client.WebSocketState;

/**
 * Created by Михаил on 10.07.2016.
 */
public interface IWSStatusListener {
    void onUpdateWSStatus(WebSocketState state);
}
