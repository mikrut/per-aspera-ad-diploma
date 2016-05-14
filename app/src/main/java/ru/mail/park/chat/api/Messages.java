package ru.mail.park.chat.api;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.net.Proxy;
import java.text.ParseException;
import java.util.List;

import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import ru.mail.park.chat.database.PreferenceConstants;
import ru.mail.park.chat.message_interfaces.IChatListener;
import ru.mail.park.chat.message_interfaces.IGroupCreateListener;
import ru.mail.park.chat.message_interfaces.IMessageSender;
import ru.mail.park.chat.models.AttachedFile;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;
import ru.mail.park.chat.models.OwnerProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class Messages extends ApiSection implements IMessageSender {
    private static final int TIMEOUT = 0; // Don't close the socket
    private WebSocket ws;

    private IChatListener chatListener;
    private IGroupCreateListener groupCreateListener;

    private final OwnerProfile profile;

    private enum Method {
        SEND,
        CHAT_CREATE
    };
    private Method lastUsed = null;

    private String getUrl() {
        String server = "http://p30480.lab1.stud.tech-mail.ru/ws/";
        String id = profile.getUid();

        return server + "?idUser=" + id;
    }

    public Messages(@NonNull final Activity context) throws IOException {
        super(context);

        profile = new OwnerProfile(context);
        WebSocketFactory wsFactory = new WebSocketFactory();

        ProxySettings proxySettings = wsFactory.getProxySettings();
        boolean torStart = OrbotHelper.requestStartTor(context);
        if (torStart) {
            NetCipher.setProxy(NetCipher.ORBOT_HTTP_PROXY);
            Proxy netCipherProxy = NetCipher.getProxy();
            Log.v(Messages.class.getSimpleName(), netCipherProxy.address().toString());

            String[] ipPort = netCipherProxy.address().toString().split(":");
            proxySettings.setHost(ipPort[0].substring(1));
            proxySettings.setPort(Integer.valueOf(ipPort[1]));
        } else {
            boolean onlyTorIsAllowed = PreferenceManager
                    .getDefaultSharedPreferences(context)
                    .getBoolean(PreferenceConstants.SECURITY_PARANOID_N, true);

            if (onlyTorIsAllowed) {
                throw new IOException("Cannot establish TOR connection");
            }
        }

        ws =    wsFactory
                .setConnectionTimeout(TIMEOUT)
                .createSocket(getUrl())
                .addListener(new WebSocketAdapter() {
                    @Override
                    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
                        super.onConnectError(websocket, exception);
                        Log.e("connect error", exception.getLocalizedMessage());
                    }

                    @Override
                    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
                        Log.v("disconnected", closedByServer ? "by server" : "by itself");
                    }

                    @Override
                    public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
                        super.onSendError(websocket, cause, frame);
                        Log.e("send error", cause.getLocalizedMessage());
                    }

                    public void onTextMessage(WebSocket websocket, final String message) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.v(Messages.class.getSimpleName(), message);
                                    JSONObject jsonIncome = new JSONObject(message);

                                    String method;
                                    if (jsonIncome.has("method")) {
                                        method = jsonIncome.getString("method");
                                    } else if (jsonIncome.has("typePush")) {
                                        method = jsonIncome.getString("typePush");
                                    } else {
                                        if (lastUsed != null) {
                                            switch (lastUsed) {
                                                case CHAT_CREATE:
                                                    method = "createChats";
                                                    break;
                                                case SEND:
                                                default:
                                                    method = "SEND";
                                                    break;
                                            }
                                        } else {
                                            method = "SEND";
                                        }

                                        // FIXME: restore this throw when backend is fixed
                                        // throw new IOException("No method field in server response");
                                    }

                                    int status = jsonIncome.getInt("status");

                                    if (status != 200)
                                        throw new IOException("Wrong income status: " + String.valueOf(status));

                                    if (chatListener != null) {
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
                                            case "createChats":
                                                dispatchCreateChats(jsonIncome);
                                                break;
                                            case "writeMessage":
                                                dispatchWriteMessage(jsonIncome);
                                        }
                                    }
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });


                    }
                })
                .connectAsynchronously();
    }

    private void dispatchCreateChats(JSONObject jsonIncome) throws JSONException {
        Chat chat = new Chat(jsonIncome.getJSONObject("data"), getContext());
        if (groupCreateListener != null) {
            groupCreateListener.onChatCreated(chat);
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
        if (chatListener != null)
            chatListener.onActionDeleteMessage(mid);
    }

    private void dispatchGet(JSONObject income) {

    }

    private void dispatchWriteMessage(JSONObject income) throws JSONException {
        JSONObject data = income.getJSONObject("data");

        String cid = data.getString("idChat");
        JSONObject user = data.getJSONObject("user");
        try {
            Contact contact = new Contact(user, getContext());
            chatListener.onWrite(cid, contact);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String cid, Message message) {
        reconnect();

        JSONObject jsonRequest = new JSONObject();
        JSONObject data = new JSONObject();
        lastUsed = Method.SEND;

        try {
            jsonRequest.put("controller", "Messages");
            jsonRequest.put("method", "send");
            jsonRequest.put("data", data);
            data.put(ApiSection.AUTH_TOKEN_PARAMETER_NAME, profile.getAuthToken());
            data.put("idRoom", cid);
            data.put("textMessage", message.getMessageBody());

            List<AttachedFile> files = message.getFiles();
            if (files != null && files.size() > 0) {
                JSONArray listIdFile = new JSONArray();
                for (AttachedFile file : files) {
                    listIdFile.put(file.getFileID());
                }
                data.put("listIdFile", listIdFile);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        Log.d(Messages.class.getSimpleName(), jsonRequest.toString());
        ws.sendText(jsonRequest.toString());
    }

    public void sendFirstMessage(String uid, Message message) {
        reconnect();

        StringBuilder b = new StringBuilder();
        for (char c : message.getMessageBody().toCharArray()) {
            if (c >= 128)
                b.append("\\u").append(String.format("%04X", (int) c));
            else
                b.append(c);
        }
        String messageBody = b.toString();

        JSONObject jsonRequest = new JSONObject();
        JSONObject data = new JSONObject();
        lastUsed = Method.SEND;

        try {
            jsonRequest.put("controller", "Messages");
            jsonRequest.put("method", "sendFirst");
            jsonRequest.put("data", data);
            data.put(ApiSection.AUTH_TOKEN_PARAMETER_NAME, profile.getAuthToken());
            data.put("idUser", uid);
            data.put("textMessage", messageBody);

            List<AttachedFile> files = message.getFiles();
            if (files != null && files.size() > 0) {
                JSONArray listIdFile = new JSONArray();
                for (AttachedFile file : files) {
                    listIdFile.put(file.getFileID());
                }
                data.put("listIdFile", listIdFile);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

        Log.v(Messages.class.getSimpleName(), jsonRequest.toString());
        ws.sendText(jsonRequest.toString());
    }

    public void createGroupChat(String title, List<String> userIDs) {
        reconnect();
        JSONObject jsonRequest = new JSONObject();
        JSONObject data = new JSONObject();
        JSONArray idUsers = new JSONArray();
        lastUsed = Method.CHAT_CREATE;

        try {
            jsonRequest.put("controller", "Chats");
            jsonRequest.put("method", "create");
            data.put(ApiSection.AUTH_TOKEN_PARAMETER_NAME, profile.getAuthToken());
            data.put("nameChat", title);
            data.put("idUsers", idUsers);
            jsonRequest.put("data", data);
            for (String uid : userIDs) {
                idUsers.put(Integer.valueOf(uid));
            }
            idUsers.put(Integer.valueOf(profile.getUid()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v(Messages.class.getSimpleName(), jsonRequest.toString());
        ws.sendText(jsonRequest.toString());
    }

    @Override
    public void write(@NonNull String cid) {
        reconnect();
        JSONObject jsonRequest = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            jsonRequest.put("controller", "Messages");
            jsonRequest.put("method", "write");
            data.put(ApiSection.AUTH_TOKEN_PARAMETER_NAME, profile.getAuthToken());
            data.put("idChat", cid);
            jsonRequest.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v(Messages.class.getSimpleName(), jsonRequest.toString());
        ws.sendText(jsonRequest.toString());
    }

    public void deleteMessage(int mid, String cid) {
        reconnect();

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
        reconnect();

        JSONObject jsonData = new JSONObject();

        try {
            jsonData.put("method", "GET");
            jsonData.put("cid", cid);
        } catch(JSONException e) {
            e.printStackTrace();
        }

     //   ws.sendText(jsonData.toString());
    }

    public void disconnect() {
        ws.disconnect();
    }

    public boolean isStateOK() {
        return !(ws.getState().equals(WebSocketState.CLOSED) || ws.getState().equals(WebSocketState.CLOSING));
    }

    public void reconnect() {
        if (!isStateOK()) {
            try {
                ws = ws.recreate();
                ws.connectAsynchronously();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(Messages.class.getSimpleName(), ws.getState().toString());
    }

    public void setGroupCreateListener(IGroupCreateListener groupCreateListener) {
        this.groupCreateListener = groupCreateListener;
    }

    public void setChatListener(IChatListener chatListener) {
        this.chatListener = chatListener;
    }
}
