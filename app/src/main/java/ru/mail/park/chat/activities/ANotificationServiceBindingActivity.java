package ru.mail.park.chat.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.R;
import ru.mail.park.chat.api.websocket.DispatcherOfDialog;
import ru.mail.park.chat.api.websocket.DispatcherOfGroupCreate;
import ru.mail.park.chat.api.websocket.DispatcherOfGroupEdit;
import ru.mail.park.chat.api.websocket.IChatListener;
import ru.mail.park.chat.api.websocket.IGroupCreateListener;
import ru.mail.park.chat.api.websocket.IGroupEditListener;
import ru.mail.park.chat.api.websocket.IWSStatusListener;
import ru.mail.park.chat.api.websocket.NotificationService;
import ru.mail.park.chat.loaders.images.ImageDownloadManager;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;

/**
 * Created by mikrut on 13.07.16.
 */
public abstract class ANotificationServiceBindingActivity
        extends AppCompatActivity
        implements IChatListener {
    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, NotificationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private NotificationService notificationService;
    private DispatcherOfDialog dispatcherOfDialog;
    private boolean bound = false;
    protected Handler uiHandler = new Handler();

    public NotificationService getNotificationService() {
        return notificationService;
    }

    private void removeDispatchersNoOverride(NotificationService notificationService) {
        notificationService.removeDispatcher(dispatcherOfDialog);

        removeDispatchers(notificationService);
    }

    private void addDispatchersNoOverride(NotificationService notificationService) {
        dispatcherOfDialog = new DispatcherOfDialog(ANotificationServiceBindingActivity.this);
        dispatcherOfDialog.setChatListener(ANotificationServiceBindingActivity.this);
        notificationService.addDispatcher(dispatcherOfDialog, uiHandler);

        addDispatchers(notificationService);
    }

    public void addDispatchers(NotificationService notificationService) {

    }

    public void removeDispatchers(NotificationService notificationService) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            removeDispatchersNoOverride(notificationService);
            
            unbindService(mConnection);
            bound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            NotificationService.NotificationBinder binder =
                    (NotificationService.NotificationBinder) service;
            notificationService = binder.getService();
            addDispatchersNoOverride(notificationService);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    public void onIncomeMessage(JSONObject message) {
        try {
            Message incomeMsg = new Message(message, this, null);

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
        } catch (JSONException ignore) {}
    }

    @Override
    public void onAcknowledgeSendMessage(JSONObject message) {

    }

    @Override
    public void onActionDeleteMessage(int mid) {

    }

    @Override
    public void onGetHistoryMessages(ArrayList<Message> msg_list) {

    }

    @Override
    public void onWrite(String cid, Contact user) {

    }
}
