package ru.mail.park.chat.api.websocket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ru.mail.park.chat.models.Chat;

/**
 * Created by mikrut on 13.07.16.
 */
public class DispatcherOfGroupCreate implements IDispatcher {
    private static final String TAG = DispatcherOfGroupCreate.class.getSimpleName();

    @Nullable
    private IGroupCreateListener groupCreateListener;
    @NonNull
    private final Context context;

    public DispatcherOfGroupCreate(@NonNull Context context) {
        this.context = context;
    }

    @Override
    public void dispatchJSON(@NonNull String method, JSONObject jsonIncome) {
        try {
            switch (method) {
                case "createChats":
                    dispatchCreateChats(jsonIncome);
                    break;
            }
        } catch (JSONException e) {
            Log.e(TAG + ".dispatchJSON", e.getLocalizedMessage());
        }
    }

    private void dispatchCreateChats(JSONObject jsonIncome) throws JSONException {
        Chat chat = new Chat(jsonIncome.getJSONObject("data"), getContext());
        if (groupCreateListener != null) {
            groupCreateListener.onChatCreated(chat);
        }
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    public void setGroupCreateListener(@Nullable IGroupCreateListener groupCreateListener) {
        this.groupCreateListener = groupCreateListener;
    }
}
