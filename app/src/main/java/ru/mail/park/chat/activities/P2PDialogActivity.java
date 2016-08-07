package ru.mail.park.chat.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;

import ru.mail.park.chat.api.p2p.P2PService;
import ru.mail.park.chat.api.websocket.IMessageSender;
import ru.mail.park.chat.models.Message;

/**
 * Created by Михаил on 24.04.2016.
 */
public class P2PDialogActivity extends DialogActivity {
    public static final String PORT_ARG = P2PDialogActivity.class.getCanonicalName() + ".PORT_ARG";
    public static final String HOST_ARG = P2PDialogActivity.class.getCanonicalName() + ".HOST_ARG";

    private boolean requestedService = false;

    @Override
    protected void onResume() {
        super.onResume();
        attachFile.setVisibility(View.GONE);
    }

    @Override
    protected IMessageSender getMessageSender() {
        if (!requestedService) {
            requestedService = true;

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                int port = extras.getInt(PORT_ARG, -1);
                String host = extras.getString(HOST_ARG);

                if (port != -1 && host != null) {
                    P2PService service = getP2PService();
                    if (service != null)
                        service.startClient(host, port);
                    return null;
                }
            }
            return null;
        } else {
            return getP2PService().getConnection();
        }
    }

    @Override
    protected void sendMessage(@NonNull Message message) {
        IMessageSender sender = getMessageSender();
        if (sender != null) {
            sender.sendMessage(null, message);
        }
    }


    // FIXME: Use string resources
    @Override
    public void onConnectionBreak() {
        new AlertDialog.Builder(this)
                .setTitle("No connection")
                .setMessage("The connection was cancelled.")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
    }
}
