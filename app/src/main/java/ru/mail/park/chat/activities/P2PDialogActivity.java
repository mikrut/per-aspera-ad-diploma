package ru.mail.park.chat.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.neovisionaries.ws.client.WebSocketState;

import ru.mail.park.chat.api.p2p.IP2PConnectionStatusListener;
import ru.mail.park.chat.api.p2p.P2PConnection;
import ru.mail.park.chat.api.p2p.P2PService;
import ru.mail.park.chat.api.websocket.IMessageSender;
import ru.mail.park.chat.database.ContactsHelper;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;

/**
 * Created by Михаил on 24.04.2016.
 */
public class P2PDialogActivity
        extends DialogActivity
        implements IP2PConnectionStatusListener {
    public static final String PORT_ARG = P2PDialogActivity.class.getCanonicalName() + ".PORT_ARG";
    public static final String HOST_ARG = P2PDialogActivity.class.getCanonicalName() + ".HOST_ARG";

    private Contact otherSide;

    private final String P2P_STATUS_TITLE = "P2P Connection";
    private AlertDialog dialog;
    private AlertDialog finishDialog;

    private boolean requestedService = false;
    private boolean inFront = false;

    @Override
    protected void onResume() {
        super.onResume();
        inFront = true;
        attachFile.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        dialog = new AlertDialog.Builder(this)
                .setTitle(P2P_STATUS_TITLE)
                .setMessage("Starting a connection")
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .create();
        dialog.show();
    }

    @Override
    protected void onSetP2PService(@NonNull P2PService p2pService) {
        getMessageSender();
        P2PConnection p2PConnection = p2pService.getConnection();
        if (p2PConnection != null && p2PConnection.isConnected()  && dialog != null) {
            dialog.dismiss();
            dialog = null;

            p2PConnection.addListener(this);
            p2PConnection.setP2PEventListener(this);

            ContactsHelper helper = new ContactsHelper(this);
            otherSide = helper.getContact(p2PConnection.getDestinationUID());
            helper.close();

            dialogActionBar.setProgress(false);
            if (otherSide != null) {
                dialogActionBar.setTitle(otherSide.getContactTitle());
            }
            dialogActionBar.setSubtitle("Connected");
        }
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
                    if (service != null) {
                        service.startClient(host, port, this);
                    } else {
                        requestedService = false;
                    }
                    return null;
                }
            }
            return null;
        } else {
            return getP2PService().getConnection();
        }
    }

    @Override
    public void onUpdateWSStatus(WebSocketState state) {}

    @Override
    protected void sendMessage(@NonNull Message message) {
        message.setMid(String.valueOf(message.getUniqueID()));
        IMessageSender sender = getMessageSender();
        if (sender != null) {
            sender.sendMessage(null, message);
        }
    }

    @Override
    public void onConnectionEstablished(String fromUid) {
        if (inFront) {
            dialog.dismiss();
            dialog = null;

            ContactsHelper helper = new ContactsHelper(this);
            otherSide = helper.getContact(fromUid);
            helper.close();

            dialogActionBar.setProgress(false);
            if (otherSide != null) {
                dialogActionBar.setTitle(otherSide.getContactTitle());
            }
            dialogActionBar.setSubtitle("Connected");
        }

        P2PService p2PService = getP2PService();
        if (p2PService != null) {
            P2PConnection connection = p2PService.getConnection();
            if (connection != null) {
                connection.addListener(this);
            }
        }
    }

    @Override
    public void onConnectionStatusChange(String status) {
        if (inFront && dialog != null) {
            dialog.setMessage(status);
        }

        P2PService p2PService = getP2PService();
        if (p2PService != null) {
            P2PConnection connection = p2PService.getConnection();
            if (connection != null) {
                connection.setP2PEventListener(this);
                connection.addListener(this);
            }
        }
    }

    @Override
    protected void onPause() {
        inFront = false;
        super.onPause();
    }

    // FIXME: Use string resources
    @Override
    public void onConnectionBreak() {
        if (inFront) {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }

            finishDialog = new AlertDialog.Builder(this)
                    .setTitle("No connection")
                    .setMessage("The connection was cancelled.")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            }).create();
            finishDialog.show();
        }
    }
}
