package ru.mail.park.chat.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

import info.guardianproject.netcipher.proxy.OrbotHelper;
import ru.mail.park.chat.R;
import ru.mail.park.chat.api.p2p.IP2PEventListener;
import ru.mail.park.chat.api.p2p.P2PService;
import ru.mail.park.chat.api.websocket.DispatcherOfDialog;
import ru.mail.park.chat.api.websocket.IChatListener;
import ru.mail.park.chat.api.websocket.NotificationService;
import ru.mail.park.chat.database.ContactsHelper;
import ru.mail.park.chat.database.PreferenceConstants;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by mikrut on 13.07.16.
 */
public abstract class ANotificationServiceBindingActivity
        extends AppCompatActivity
        implements IChatListener, IP2PEventListener {
    private static final String TAG = ANotificationServiceBindingActivity.class.getSimpleName();
    public final static int LISTENER_DEFAULT_PORT = 8275;

    @Override
    protected void onStart() {
        super.onStart();

        Intent notificationsIntent = new Intent(this, NotificationService.class);
        bindService(notificationsIntent, mNotificationConnection, Context.BIND_AUTO_CREATE);

        if (OrbotHelper.isOrbotInstalled(this)) {
            SharedPreferences preferences =
                    getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_PRIVATE);
            String hostname = preferences.getString(PreferenceConstants.P2P_HOSTNAME, null);
            if (hostname == null) {
                OrbotHelper.requestHiddenServiceOnPort(this, LISTENER_DEFAULT_PORT);
            } else {
                startP2PService();
            }
        }
    }

    private boolean boundToNotifications = false;
    private NotificationService notificationService;
    private DispatcherOfDialog dispatcherOfDialog;

    private void startP2PService() {
        if (OrbotHelper.isOrbotInstalled(this)) {
            Intent p2pIntent = new Intent(this, P2PService.class);
            p2pIntent.setAction(P2PService.ACTION_START_SERVER);
            bindService(p2pIntent, mP2PConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private P2PService p2pService;
    private boolean boundToP2P = false;

    protected P2PService getP2PService() {
        return p2pService;
    }

    protected Handler uiHandler = new Handler();

    @Override
    public synchronized void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.v(TAG, "received onion hostname");
        if (requestCode == OrbotHelper.HS_REQUEST_CODE) {
            if (intent != null) {
                String localHostname = intent.getStringExtra("hs_host");

                if (localHostname != null) {
                    Log.i(TAG, "P2P local hostname: " +localHostname);

                    SharedPreferences preferences =
                            getSharedPreferences(PreferenceConstants.PREFERENCE_NAME, MODE_APPEND);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(PreferenceConstants.P2P_HOSTNAME, localHostname);
                    editor.apply();

                    startP2PService();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    private void removeDispatchersNoOverride(NotificationService notificationService) {
        notificationService.removeDispatcher(dispatcherOfDialog);
        Log.d(TAG, "removeDispatchers");

        removeDispatchers(notificationService);
    }

    private void addDispatchersNoOverride(NotificationService notificationService) {
        dispatcherOfDialog = new DispatcherOfDialog(ANotificationServiceBindingActivity.this);
        dispatcherOfDialog.setChatListener(ANotificationServiceBindingActivity.this);
        notificationService.addDispatcher(dispatcherOfDialog, uiHandler);

        Log.d(TAG, "addDispatchers");
        addDispatchers(notificationService);
    }

    public void addDispatchers(NotificationService notificationService) {

    }

    public void removeDispatchers(NotificationService notificationService) {

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (p2pService != null) {
            p2pService.onActivityPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (boundToNotifications) {
            removeDispatchersNoOverride(notificationService);
            
            unbindService(mNotificationConnection);
            boundToNotifications = false;
            notificationService = null;
            dispatcherOfDialog = null;
        }

        if (boundToP2P) {
            unbindService(mP2PConnection);
            boundToP2P = false;
            p2pService = null;
        }
    }

    private ServiceConnection mP2PConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            P2PService.P2PServiceSingletonBinder binder =
                    (P2PService.P2PServiceSingletonBinder) iBinder;
            p2pService = binder.getService();
            boundToP2P = true;
            p2pService.setP2PEventListener(ANotificationServiceBindingActivity.this);
            onSetP2PService(p2pService);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            boundToP2P = false;
        }
    };

    protected void onSetP2PService(@NonNull P2PService p2pService) {

    }

    private ServiceConnection mNotificationConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            NotificationService.NotificationBinder binder =
                    (NotificationService.NotificationBinder) service;
            notificationService = binder.getService();
            addDispatchersNoOverride(notificationService);
            boundToNotifications = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundToNotifications = false;
        }
    };

    @Override
    public void onIncomeMessage(Message incomeMsg) {
        OwnerProfile owner = new OwnerProfile(this);
        if (!owner.getUid().equals(incomeMsg.getUid())) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

            notificationBuilder.setSmallIcon(R.drawable.ic_message_black_24dp)
                    .setContentTitle(incomeMsg.getTitle())
                    .setContentText(incomeMsg.getMessageBody());
            Intent intent = new Intent(this, DialogActivity.class);
            intent.putExtra(DialogActivity.CHAT_ID, incomeMsg.getCid());
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(pendingIntent);

            Notification notification = notificationBuilder.build();
            notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, notification);
        }
    }

    @Override
    public void onAcknowledgeSendMessage(Message message) {

    }

    @Override
    public void onActionDeleteMessage(int mid) {

    }

    @Override
    public void onWrite(String cid, Contact user) {

    }

    private AlertDialog incoming;

    // FIXME: Use string resources
    @Override
    public void onConnectionEstablished(String fromUid) {
        Log.d(TAG + ".onConnection", "UID: " + fromUid);

        ContactsHelper helper = new ContactsHelper(this);
        Contact contact = helper.getContact(fromUid);
        helper.close();

        if (contact != null) {
            if (incoming != null) {
                incoming.dismiss();
            }
            incoming = new AlertDialog.Builder(this)
                .setTitle("Incoming P2P connection")
                .setMessage("Incoming P2P connection from user " + contact.getLogin())
                .setCancelable(false)
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(ANotificationServiceBindingActivity.this, P2PDialogActivity.class);
                        startActivity(intent);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        p2pService.getConnection().closeStreams();
                        dialogInterface.dismiss();
                    }
                })
                .create();
            incoming.show();
        } else {
            p2pService.getConnection().closeStreams();
        }
    }

    @Override
    public void onConnectionBreak() {
        Log.d(TAG + ".onConnectionBreak", "break of connection");
        if (incoming != null) {
            incoming.dismiss();
            incoming = null;
        }
    }
}
