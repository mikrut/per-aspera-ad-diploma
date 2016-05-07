package ru.mail.park.chat.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import ru.mail.park.chat.api.P2PServerListener;
import ru.mail.park.chat.api.P2PService;
import ru.mail.park.chat.message_interfaces.IMessageSender;
import ru.mail.park.chat.models.Message;

/**
 * Created by Михаил on 24.04.2016.
 */
public class P2PDialogActivity extends DialogActivity {
    public static final String PORT_ARG = P2PDialogActivity.class.getCanonicalName() + ".PORT_ARG";
    public static final String HOST_ARG = P2PDialogActivity.class.getCanonicalName() + ".HOST_ARG";

    public final static int LISTENER_DEFAULT_PORT = 8275;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            P2PService.P2PServiceSingletonBinder binder = (P2PService.P2PServiceSingletonBinder) service;
            messages = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected IMessageSender getMessageSender() throws IOException {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int port = extras.getInt(PORT_ARG, -1);
            String host = extras.getString(HOST_ARG);

            if (port != -1 && host != null) {
                Intent intent = new Intent(this, P2PService.class);
                intent.setAction(P2PService.ACTION_START_CLIENT);
                intent.putExtra(P2PService.DESTINATION_URL, host);
                intent.putExtra(P2PService.DESTINATION_PORT, port);

                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                return null;
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

                    Intent serviceIntent = new Intent(this, P2PService.class);
                    serviceIntent.setAction(P2PService.ACTION_START_SERVER);
                    bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    protected void sendMessage(@NonNull String messageBody) {
        if (messages != null) {
            Message message = new Message(messageBody, this);
            messages.sendMessage(null, message);
        }
    }
}
