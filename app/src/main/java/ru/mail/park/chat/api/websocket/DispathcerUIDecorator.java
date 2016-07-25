package ru.mail.park.chat.api.websocket;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONObject;

/**
 * Created by mikrut on 13.07.16.
 */
public class DispathcerUIDecorator implements IDispatcher {
    @NonNull
    private final IDispatcher dispatcher;
    @Nullable
    private final Handler uiHandler;

    public DispathcerUIDecorator(@NonNull IDispatcher decorable, @Nullable Handler uiHandler) {
        this.dispatcher = decorable;
        this.uiHandler  = uiHandler;
    }

    @Override
    public void dispatchJSON(@NonNull final String method, final JSONObject jsonIncome) {
        if (uiHandler != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    dispatcher.dispatchJSON(method, jsonIncome);
                }
            });
        } else {
            dispatcher.dispatchJSON(method, jsonIncome);
        }
    }
}
