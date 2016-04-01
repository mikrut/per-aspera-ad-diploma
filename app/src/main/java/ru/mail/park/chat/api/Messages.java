package ru.mail.park.chat.api;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import ru.mail.park.chat.activities.DialogActivity;
import ru.mail.park.chat.activities.tasks.IncomeMessageTask;
import ru.mail.park.chat.message_income.IMessageReaction;
import ru.mail.park.chat.models.Message;
import ru.mail.park.chat.models.OwnerProfile;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class Messages extends ApiSection {
    private static final int TIMEOUT = 5000;
    private WebSocket ws;
    private final IMessageReaction taskListener;
    private final Context taskContext;

    private String getUrl() {
        String server = "http://p30480.lab1.stud.tech-mail.ru/ws/";
        String id = new OwnerProfile(taskContext).getUid();
        String result = server + "?idUser=" + id;

        return result;
    }

    public Messages(@NonNull final Activity context, IMessageReaction listener) {
        super(context);

        this.taskContext = context;
        this.taskListener = listener;

        try {
            ws = new WebSocketFactory()
                    .setConnectionTimeout(TIMEOUT)
                    .createSocket(getUrl())
                    .addListener(new WebSocketAdapter() {
                        public void onTextMessage(WebSocket websocket, final String message) {

                            // new IncomeMessageTask(taskContext, taskListener).execute(message);
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    IMessageReaction listener = taskListener;
                                    String income = message;
                                    JSONObject jsonIncome = new JSONObject();
                                    String method = "";
                                    int mid = 0;
                                    ArrayList<Message> msgList = new ArrayList<>();

                                    try {
                                        jsonIncome = new JSONObject(income);
                                        method = jsonIncome.getString("method");
                                        if (jsonIncome.has("mid")) {
                                            mid = jsonIncome.getInt("mid");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    switch (method) {
                                        case "SEND":
                                            listener.onActionSendMessage(income);
                                            break;
                                        case "DELETE":
                                            listener.onActionDeleteMessage(mid);
                                            break;
                                        case "GET":
                                            try {
                                                JSONArray jsonMsgArray = jsonIncome.getJSONArray("messages");

                                                for (int i = 0; i < jsonMsgArray.length(); i++) {
                                                    JSONObject item = jsonMsgArray.getJSONObject(i);
                                                    Message msg = new Message(item);

                                                    msgList.add(msg);
                                                }

                                                listener.onGetHistoryMessages(msgList);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            break;
                                        case "POST":
                                        case "INCOME":
                                            listener.onIncomeMessage(income);
                                            break;
                                        case "COMET":
                                            break;
                                    }
                                }
                            });

                        }
                    })
                    .connectAsynchronously();
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

    public void deleteMessage(int mid, String cid) {
        JSONObject jsonData = new JSONObject();

        try {
            jsonData.put("method", "DELETE");
            jsonData.put("mid", mid);
            jsonData.put("cid", cid);
        } catch(JSONException e) {
            e.printStackTrace();
        }

    //    ws.sendText(jsonData.toString());
    }

    public void getHistory(String cid) {
        JSONObject jsonData = new JSONObject();

        try {
            jsonData.put("method", "GET");
            jsonData.put("cid", cid);
        } catch(JSONException e) {
            e.printStackTrace();
        }

     //   ws.sendText(jsonData.toString());
    }
}
