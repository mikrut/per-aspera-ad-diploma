package ru.mail.park.chat.api.websocket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.neovisionaries.ws.client.WebSocketState;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import ru.mail.park.chat.R;
import ru.mail.park.chat.loaders.MessagesLoader;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;

/**
 * Created by Михаил on 10.07.2016.
 */
public class NotificationService
        extends Service {
    private final IBinder mBinder = new NotificationBinder();

    /*
    private final Set<IChatListener> chatListeners = new HashSet<>();
    private final Set<IGroupCreateListener> groupCreateListeners = new HashSet<>();
    private final Set<IGroupEditListener> groupEditListeners = new HashSet<>();
    private final Set<IWSStatusListener> wsStatusListeners = new HashSet<>();
    */

    private Timer schedulerTimer;
    private static final int RECONNECT_DELAY_MILLIS = 1000;

    public class NotificationBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Messages messages;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            messages = new Messages(this);
            setTimer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        messages.reconnect();
        setTimer();
    }

    private void setTimer() {
        schedulerTimer = new Timer();
        schedulerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!messages.isConnected()) {
                    messages.reconnect();
                }
            }
        }, RECONNECT_DELAY_MILLIS, RECONNECT_DELAY_MILLIS);
    }

    public Messages getMessages() {
        return messages;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        schedulerTimer.cancel();
        messages.disconnect();
        return super.onUnbind(intent);
    }

    public void addDispater(@NonNull IDispatcher dispatcher) {
        addDispatcher(dispatcher, null);
    }

    public void addDispatcher(@NonNull IDispatcher dispatcher, @Nullable Handler uiHandler) {
        if (uiHandler != null)
            dispatcher = new DispathcerUIDecorator(dispatcher, uiHandler);
        messages.addDispatcher(dispatcher);
    }

    public void removeDispatcher(IDispatcher dispatcher) {
        messages.removeDispatcher(dispatcher);
    }

    // Some helper methods
    /*
    public void addChatListener(@NonNull IChatListener listener) {
        chatListeners.add(listener);
    }
    
    public void addGroupCreateListener(@NonNull IGroupCreateListener listener) {
        groupCreateListeners.add(listener);
    }
    
    public void addGroupEditListener(@NonNull IGroupEditListener listener) {
        groupEditListeners.add(listener);
    }
    
    public void addWSStatusListener(@NonNull IWSStatusListener listener) {
        wsStatusListeners.add(listener);
    }

    public void removeChatListener(@NonNull IChatListener listener) {
        chatListeners.remove(listener);
    }

    public void removeGroupCreateListener(@NonNull IGroupCreateListener listener) {
        groupCreateListeners.remove(listener);
    }

    public void removeGroupEditListener(@NonNull IGroupEditListener listener) {
        groupEditListeners.remove(listener);
    }

    public void removeWSStatusListener(@NonNull IWSStatusListener listener) {
        wsStatusListeners.remove(listener);
    }

    @Override
    public void onIncomeMessage(JSONObject message) {
        for (IChatListener chatListener : chatListeners) {
            chatListener.onIncomeMessage(message);
        }
    }

    @Override
    public void onAcknowledgeSendMessage(JSONObject message) {
        for (IChatListener chatListener : chatListeners) {
            chatListener.onAcknowledgeSendMessage(message);
        }
    }

    @Override
    public void onActionDeleteMessage(int mid) {
        for (IChatListener chatListener : chatListeners) {
            chatListener.onActionDeleteMessage(mid);
        }
    }

    @Override
    public void onGetHistoryMessages(ArrayList<Message> msg_list) {
        for (IChatListener chatListener : chatListeners) {
            chatListener.onGetHistoryMessages(msg_list);
        }
    }

    @Override
    public void onWrite(String cid, Contact user) {
        for (IChatListener chatListener : chatListeners) {
            chatListener.onWrite(cid, user);
        }
    }

    @Override
    public void onChatCreated(Chat chat) {
        for (IGroupCreateListener groupCreateListener : groupCreateListeners) {
            groupCreateListener.onChatCreated(chat);
        }
    }

    @Override
    public void onAddUser(@NonNull String cid, @NonNull Contact user) {
        for (IGroupEditListener groupEditListener : groupEditListeners) {
            groupEditListener.onAddUser(cid, user);
        }
    }

    @Override
    public void onUpdateName(@NonNull String cid, @NonNull String chatName) {
        for (IGroupEditListener groupEditListener : groupEditListeners) {
            groupEditListener.onUpdateName(cid, chatName);
        }
    }

    @Override
    public void onUpdateWSStatus(WebSocketState state) {
        for (IWSStatusListener wsStatusListener : wsStatusListeners) {
            wsStatusListener.onUpdateWSStatus(state);
        }
    }
    */
}
