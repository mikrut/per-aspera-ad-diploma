package ru.mail.park.chat.api.websocket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Михаил on 10.07.2016.
 */
public class NotificationService extends Service {
    private final IBinder mBinder = new NotificationBinder();

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

    private WSConnection wsConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            wsConnection = new WSConnection(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private interface DispatchMethod {
        void dispatchMethod(JSONObject income);
    }
}
