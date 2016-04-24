package ru.mail.park.chat.activities;

import android.os.Bundle;

import java.io.IOException;

import ru.mail.park.chat.activities.DialogActivity;
import ru.mail.park.chat.api.P2PServerListener;
import ru.mail.park.chat.message_interfaces.IMessageSender;

/**
 * Created by Михаил on 24.04.2016.
 */
public class P2PDialogActivity extends DialogActivity {
    public static final String PORT_ARG = P2PDialogActivity.class.getCanonicalName() + ".PORT_ARG";
    public static final String HOST_ARG = P2PDialogActivity.class.getCanonicalName() + ".HOST_ARG";

    final static int LISTENER_DEFAULT_PORT = 8081;

    @Override
    protected IMessageSender getMessageSender() throws IOException {
        Bundle extras = getIntent().getExtras();
        int port = extras.getInt(PORT_ARG, -1);
        String host = extras.getString(HOST_ARG);
        P2PServerListener.DestinationParams destinationParams = null;
        if (port != -1 && host != null) {
            destinationParams = new P2PServerListener.DestinationParams();
            destinationParams.destinationName = host;
            destinationParams.destinationPort = port;
        }
        P2PServerListener p2PServerListener = new P2PServerListener(this, destinationParams);
        if (destinationParams != null) {
            p2PServerListener.execute();
        } else {
            p2PServerListener.execute(LISTENER_DEFAULT_PORT);
        }
        return p2PServerListener;
    }
}
