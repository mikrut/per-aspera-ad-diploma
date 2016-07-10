package ru.mail.park.chat.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import info.guardianproject.netcipher.proxy.OrbotHelper;
import ru.mail.park.chat.R;
import ru.mail.park.chat.api.p2p.P2PService;
import ru.mail.park.chat.api.websocket.IMessageSender;
import ru.mail.park.chat.models.Message;

/**
 * Created by Михаил on 24.04.2016.
 */
public class P2PDialogActivity extends DialogActivity {
    public static final String PORT_ARG = P2PDialogActivity.class.getCanonicalName() + ".PORT_ARG";
    public static final String HOST_ARG = P2PDialogActivity.class.getCanonicalName() + ".HOST_ARG";

    public final static int LISTENER_DEFAULT_PORT = 8275;

    @Override
    protected void onResume() {
        super.onResume();
        attachFile.setVisibility(View.GONE);
    }

    private ServiceConnection mConnection;
    private ServiceConnection getConnection() {
        if (mConnection == null) {
            mConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    P2PService.P2PServiceSingletonBinder binder = (P2PService.P2PServiceSingletonBinder) service;
                    P2PService p2PService = binder.getService();
                    messages = p2PService;
                    p2PService.addListener(P2PDialogActivity.this);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
        }
        return mConnection;
    }

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

                bindService(intent, getConnection(), Context.BIND_AUTO_CREATE);
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
                    TextView lastSeen = (TextView) findViewById(R.id.dialog_last_seen);
                    if (lastSeen != null)
                        lastSeen.setText(localHostname);

                    Intent serviceIntent = new Intent(this, P2PService.class);
                    serviceIntent.setAction(P2PService.ACTION_START_SERVER);
                    bindService(serviceIntent, getConnection(), Context.BIND_AUTO_CREATE);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    protected void sendMessage(@NonNull Message message) {
        if (messages != null) {
            messages.sendMessage(null, message);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mConnection != null) {
            unbindService(mConnection);
        }
        mConnection = null;
    }
}
