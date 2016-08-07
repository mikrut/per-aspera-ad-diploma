package ru.mail.park.chat.api.p2p;

/**
 * Created by mikrut on 07.08.16.
 */
public interface IP2PEventListener {
    void onConnectionEstablished(String fromUid);
    void onConnectionBreak();
}
