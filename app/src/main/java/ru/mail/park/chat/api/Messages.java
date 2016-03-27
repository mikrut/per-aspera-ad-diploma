package ru.mail.park.chat.api;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class Messages {
    private ServerConnection sConn;

    public Messages(ServerConnection sConn) {
        this.sConn = sConn;
    }

    public void sendMessage() {

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
