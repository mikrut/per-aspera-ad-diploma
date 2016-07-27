package ru.mail.park.chat.api.websocket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;

/**
 * Created by Михаил on 10.07.2016.
 */
public class DispatcherOfDialog implements IDispatcher {
    private static final String TAG = DispatcherOfDialog.class.getSimpleName();
    @Nullable
    private IChatListener chatListener;
    private Context context;

    public DispatcherOfDialog(@NonNull Context context) {
        this.context = context;
    }

    public void setChatListener(@Nullable IChatListener chatListener) {
        this.chatListener = chatListener;
    }

    public Context getContext() {
        return context;
    }

    public void dispatchJSON(@NonNull final String method, final JSONObject jsonIncome) {
        try{
            switch (method) {
                case "newMessage":
                    if (jsonIncome.has("user"))
                        dispatchNewMessage(jsonIncome);
                    else
                        dispatchAckSend(jsonIncome);
                    break;
                case "DELETE":
                    dispatchDelete(jsonIncome);
                    break;
                case "GET":
                    dispatchGet(jsonIncome);
                    break;
                case "writeMessage":
                    dispatchWriteMessage(jsonIncome);
                    break;
            }
        } catch (JSONException e) {
            Log.w(TAG + ".dispatchJSON", e.getLocalizedMessage());
        }
    }

    private void dispatchAckSend(JSONObject income) throws JSONException {
        JSONObject data = income.getJSONObject("data");

        if (chatListener != null) {
            Message message = new Message(income, getContext());
            chatListener.onAcknowledgeSendMessage(message);
        }
    }

    private void dispatchNewMessage(JSONObject income) throws JSONException {
        JSONObject data = income.getJSONObject("data");

        if (chatListener != null) {
            Message message = new Message(income, getContext());
            chatListener.onIncomeMessage(message);
        }
    }

    private void dispatchDelete(JSONObject income) throws JSONException {
        int mid = income.getInt("mid");

        if (chatListener != null)
            chatListener.onActionDeleteMessage(mid);
    }

    private void dispatchGet(JSONObject income) {

    }

    private void dispatchWriteMessage(JSONObject income) throws JSONException {
        JSONObject data = income.getJSONObject("data");

        String cid = data.getString("idChat");
        JSONObject user = data.getJSONObject("user");

        if (chatListener != null) {
            try {
                Contact contact = new Contact(user, getContext());
                chatListener.onWrite(cid, contact);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
