package ru.mail.park.chat.api;

import android.content.Context;
import android.support.annotation.NonNull;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ru.mail.park.chat.activities.DialogActivity;
import ru.mail.park.chat.activities.tasks.IncomeMessageTask;
import ru.mail.park.chat.message_income.IMessageReaction;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class Messages extends ApiSection {
    private static final int TIMEOUT = 5000;
    private static final String URL_ADDITION = "messages/message";
    private WebSocket ws;
    private final IMessageReaction taskListener;
    private final Context taskContext;

    @Override
    protected String getUrlAddition() {
        return super.getUrlAddition() + URL_ADDITION;
    }

    public Messages(@NonNull Context context, IMessageReaction listener) {
        super(context);

        this.taskContext = context;
        this.taskListener = listener;

        try {
            ws = new WebSocketFactory()
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(getUrlAddition())
                    .addListener(new WebSocketAdapter() {
                        public void onTextMessage(WebSocket websocket, String message) {
                            new IncomeMessageTask(taskContext, taskListener).execute(message);
                        }
                    })
                    .connect();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendMessage(int uid, String cid, String msg_body) throws JSONException{
        JSONObject jsonData = new JSONObject();

        try {
            jsonData.put("method", "POST");
            jsonData.put("uid", uid);
            jsonData.put("cid", cid);
            jsonData.put("msg_body", msg_body);
        } catch(JSONException e) {
            e.printStackTrace();
        }

        ws.sendText(jsonData.toString());
    }

    public void deleteMessage() {

    }

    public void getHistory() {

    }
}
