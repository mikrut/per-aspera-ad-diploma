package ru.mail.park.chat.api.websocket;

/**
 * Created by mikrut on 13.07.16.
 */
public interface IWSStatusNotifier {
    void setWsStatusListener(IWSStatusListener chatListener);
}
