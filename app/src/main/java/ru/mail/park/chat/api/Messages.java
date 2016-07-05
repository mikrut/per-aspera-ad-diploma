package ru.mail.park.chat.api;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import ru.mail.park.chat.message_interfaces.IChatListener;
import ru.mail.park.chat.message_interfaces.IGroupCreateListener;
import ru.mail.park.chat.message_interfaces.IGroupEditListener;
import ru.mail.park.chat.message_interfaces.IMessageSender;
import ru.mail.park.chat.models.AttachedFile;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class Messages extends WSConnection implements IMessageSender {
    private static final String TAG = Messages.class.getSimpleName();

    @Nullable
    private  IChatListener chatListener;
    @Nullable
    private IGroupCreateListener groupCreateListener;
    @Nullable
    private IGroupEditListener groupEditListener;

    private String anotherSideStatus = null;

    private final @NonNull Handler uiHandler;

    public Messages(@NonNull final Context context, @NonNull Handler uiHandler) throws IOException {
        super(context);
        this.uiHandler = uiHandler;
    }

    @Override
    protected void dispatchNewState(WebSocketState newState) {
        super.dispatchNewState(newState);
        if (chatListener != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    chatListener.onUpdateChatStatus(isConnected());
                }
            });
        }
    }

    @Override
    protected void dispatchJSON(@NonNull final String method, final JSONObject jsonIncome) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
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
                            case "writeMessage":
                                dispatchWriteMessage(jsonIncome);
                                break;
                        }
                    }
                    if (groupCreateListener != null) {
                        switch (method) {
                            case "createChats":
                                dispatchCreateChats(jsonIncome);
                                break;
                        }
                    }
                    if (groupEditListener != null) {
                        switch (method) {
                            case "addUser":
                                dispatchAddUser(jsonIncome);
                                break;
                            case "updateName":
                                dispatchUpdateName(jsonIncome);
                                break;
                        }
                    }
                } catch (JSONException e) {
                    Log.w(TAG + ".dispatchJSON", e.getLocalizedMessage());
                }
            }
        });
        super.dispatchJSON(method, jsonIncome);
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

        if (chatListener != null) {
            try {
                Contact contact = new Contact(user, getContext());
                chatListener.onWrite(cid, contact);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void dispatchAddUser(JSONObject income) throws JSONException {
        JSONObject data = income.getJSONObject("data");

        JSONObject user = data.getJSONObject("userNew");
        String cid = data.getString("idRoom");

        if (groupEditListener != null) {
            try {
                Contact contact = new Contact(user, getContext());
                groupEditListener.onAddUser(cid, contact);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void dispatchUpdateName(JSONObject income) throws JSONException {
        JSONObject data = income.getJSONObject("data");

        String cid = data.getString("idRoom");
        String name = data.getString("user");

        if (groupEditListener != null) {
            groupEditListener.onUpdateName(cid, name);
        }
    }

    public void sendMessage(String cid, Message message) {
        reconnect();

        JSONObject jsonRequest = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            jsonRequest.put("controller", "Messages");
            jsonRequest.put("method", "send");
            jsonRequest.put("data", data);
            data.put(ApiSection.AUTH_TOKEN_PARAMETER_NAME, profile.getAuthToken());
            data.put("idRoom", cid);
            data.put("textMessage", message.getMessageBody());
            data.put("uniqueId", message.getUniqueID());

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

        Log.d(TAG, jsonRequest.toString());
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

        Log.v(TAG, jsonRequest.toString());
        ws.sendText(jsonRequest.toString());
    }

    public void createGroupChat(String title, List<String> userIDs) {
        reconnect();
        JSONObject jsonRequest = new JSONObject();
        JSONObject data = new JSONObject();
        JSONArray idUsers = new JSONArray();

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

        Log.v(TAG, jsonRequest.toString());
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

        Log.v(TAG, jsonRequest.toString());
        ws.sendText(jsonRequest.toString());
    }

    public void addUser(String uid, String cid) {
        reconnect();
        JSONObject jsonRequest = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            jsonRequest.put("controller", "Chats");
            jsonRequest.put("method", "addUser");
            data.put(ApiSection.AUTH_TOKEN_PARAMETER_NAME, profile.getAuthToken());
            data.put("idRoom", cid);
            data.put("idUser", uid);
            jsonRequest.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ws.sendText(jsonRequest.toString());
    }

    public void updateName(String cid, String chatName) {
        reconnect();
        JSONObject jsonRequest = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            jsonRequest.put("controller", "Chats");
            jsonRequest.put("method", "updateName");
            data.put(ApiSection.AUTH_TOKEN_PARAMETER_NAME, profile.getAuthToken());
            data.put("idRoom", cid);
            data.put("nameChat", chatName);
            jsonRequest.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v(TAG + ".updateName", jsonRequest.toString());
        ws.sendText(jsonRequest.toString());
    }

    public void disconnect() {
        ws.disconnect();
    }

    public boolean isStateOK() {
        return !(ws.getState().equals(WebSocketState.CLOSED) || ws.getState().equals(WebSocketState.CLOSING));
    }

    @Override
    public boolean isConnected() {
        return ws.getState().equals(WebSocketState.OPEN);
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
    }

    public void setGroupCreateListener(IGroupCreateListener groupCreateListener) {
        this.groupCreateListener = groupCreateListener;
    }

    public void setChatListener(IChatListener chatListener) {
        this.chatListener = chatListener;
    }

    public void setGroupEditListener(@Nullable IGroupEditListener groupEditListener) {
        this.groupEditListener = groupEditListener;
    }
}
