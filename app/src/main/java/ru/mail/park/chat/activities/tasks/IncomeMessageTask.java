package ru.mail.park.chat.activities.tasks;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.mail.park.chat.message_income.IMessageReaction;
import ru.mail.park.chat.models.Message;

/**
 * Created by 1запуск BeCompact on 27.03.2016.
 */
public class IncomeMessageTask extends AsyncTask<String, Void, Void> {
    private IMessageReaction listener;

    public IncomeMessageTask(IMessageReaction listener) {
        this.listener = listener;

    }

    @Override
    protected Void doInBackground(String... params) {
        JSONObject jsonIncome = new JSONObject();
        String method = "";
        int mid = 0;
        ArrayList<Message> msgList = new ArrayList<>();

        try {
            jsonIncome = new JSONObject(params[0]);
            method = jsonIncome.getString("method");
            mid = jsonIncome.getInt("mid");
        } catch(Exception e) {
            e.printStackTrace();
        }

        switch(method) {
            case "SEND":
                listener.onActionSendMessage(params[0]);
                break;
            case "DELETE":
                listener.onActionDeleteMessage(mid);
                break;
            case "GET":
                try {
                    JSONArray jsonMsgArray = jsonIncome.getJSONArray("messages");

                    for(int i = 0; i < jsonMsgArray.length(); i++)
                    {
                        JSONObject item = jsonMsgArray.getJSONObject(i);
                        Message msg = new Message(item);

                        msgList.add(msg);
                    }

                    listener.onGetHistoryMessages(msgList);
                } catch(Exception e) {
                    e.printStackTrace();
                }
                break;
            case "INCOME":
                listener.onIncomeMessage(params[0]);
                break;
            case "COMET":
                break;
        }
        return null;
    }

    @Override
    protected void onPreExecute() {

    }
}
