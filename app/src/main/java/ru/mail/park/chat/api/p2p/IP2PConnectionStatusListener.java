package ru.mail.park.chat.api.p2p;

/**
 * Created by mikrut on 09.08.16.
 */
public interface IP2PConnectionStatusListener extends IP2PEventListener {
        void onConnectionStatusChange(String status);
}
