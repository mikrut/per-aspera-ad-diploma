package ru.mail.park.chat.api.websocket;

import android.support.annotation.NonNull;

import org.json.JSONObject;

/**
 * Created by mikrut on 13.07.16.
 */
public interface IDispatcher {
    void dispatchJSON(@NonNull final String method, final JSONObject jsonIncome);
}
