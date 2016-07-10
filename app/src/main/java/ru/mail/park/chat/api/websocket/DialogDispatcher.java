package ru.mail.park.chat.api.websocket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import ru.mail.park.chat.models.Contact;

/**
 * Created by Михаил on 10.07.2016.
 */
public class DialogDispatcher {
    private static final String TAG = DialogDispatcher.class.getSimpleName();
    @Nullable
    private IChatListener chatListener;
    private Context context;

    public DialogDispatcher(@NonNull Context context) {
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
                case "SEND":
                    dispatchSend(jsonIncome);
                    break;
                case "newMessage":
                    dispatchNewMessage(jsonIncome);
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

    private void dispatchSend(JSONObject income) throws JSONException {
        JSONObject data = income.getJSONObject("data");

        String message = data.getString("textMessage");
        String cid = data.getString("idMessage");
        String creationDate = data.getString("dtCreate");

        if (chatListener != null)
            chatListener.onAcknowledgeSendMessage(data);
    }

    private void dispatchNewMessage(JSONObject income) throws JSONException {
        JSONObject data = income.getJSONObject("data");

        String message = data.getString("textMessage");
        JSONObject user = data.getJSONObject("user");
        String cid = data.getString("idRoom");
        String creationDate = data.getString("dtCreate");

        if (chatListener != null)
            chatListener.onIncomeMessage(data);
    }

    private void dispatchDelete(JSONObject income) throws JSONException {
        int mid = income.getInt("mid");

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
