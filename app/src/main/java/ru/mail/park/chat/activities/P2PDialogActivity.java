package ru.mail.park.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import ru.mail.park.chat.api.P2PServerListener;
import ru.mail.park.chat.message_interfaces.IMessageSender;

/**
 * Created by Михаил on 24.04.2016.
 */
public class P2PDialogActivity extends DialogActivity {
    public static final String PORT_ARG = P2PDialogActivity.class.getCanonicalName() + ".PORT_ARG";
    public static final String HOST_ARG = P2PDialogActivity.class.getCanonicalName() + ".HOST_ARG";

    public final static String LISTENER_DEFAULT_HOST = "127.0.0.1";
    public final static int LISTENER_DEFAULT_PORT = 8275;

    @Override
    protected IMessageSender getMessageSender() throws IOException {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int port = extras.getInt(PORT_ARG, -1);
            String host = extras.getString(HOST_ARG);
            P2PServerListener.DestinationParams destinationParams = null;
            if (port != -1 && host != null) {
                destinationParams = new P2PServerListener.DestinationParams();
                destinationParams.destinationName = host;
                destinationParams.destinationPort = port;

                P2PServerListener p2PServerListener = new P2PServerListener(this, this, destinationParams);
                p2PServerListener.start();
                return p2PServerListener;
            }
        }

        requestHiddenService();
        return null;
    }

    private void requestHiddenService () {
        Intent nIntent = new Intent();
        nIntent.setAction(OrbotHelper.ACTION_REQUEST_HS);
        nIntent.putExtra("hs_port", LISTENER_DEFAULT_PORT);
        startActivityForResult(nIntent, OrbotHelper.HS_REQUEST_CODE);
    }

    @Override
    public synchronized void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.v("ac res", "received result");
        if (requestCode == OrbotHelper.HS_REQUEST_CODE) {
            if (intent != null) {
                String localHostname = intent.getStringExtra("hs_host");

                if (localHostname != null) {
                    Log.i("P2P local hostname", localHostname);
                    P2PServerListener p2PServerListener = new P2PServerListener(this, this, null);
                    p2PServerListener.setPort(LISTENER_DEFAULT_PORT);
                    p2PServerListener.start();
                    messages = p2PServerListener;

                    P2PServerListener.DestinationParams destinationParams =
                            new P2PServerListener.DestinationParams();
                    destinationParams.destinationName = localHostname;
                    destinationParams.destinationPort = LISTENER_DEFAULT_PORT;

                    P2PServerListener p2p = new P2PServerListener(this, this, destinationParams);
                    p2p.start();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    protected void sendMessage(@NonNull String message) {
        if (messages != null) {
            messages.sendMessage(null, message);
        }
    }
}
