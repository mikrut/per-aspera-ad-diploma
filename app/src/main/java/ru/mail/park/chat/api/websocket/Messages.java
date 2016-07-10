package ru.mail.park.chat.api.websocket;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocketState;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import ru.mail.park.chat.api.ApiSection;
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
    private WSStatusListener wsStatusListener;
    @Nullable
    private IGroupCreateListener groupCreateListener;
    @Nullable
    private IGroupEditListener groupEditListener;

    private final @NonNull Handler uiHandler;
    private final @NonNull DialogDispatcher dialogDispatcher;

    public Messages(@NonNull final Context context, @NonNull Handler uiHandler) throws IOException {
        super(context);
        this.uiHandler = uiHandler;
        dialogDispatcher = new DialogDispatcher(context);
    }

    @Override
    protected void dispatchNewState(final WebSocketState newState) {
        super.dispatchNewState(newState);
        if (wsStatusListener != null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    wsStatusListener.onUpdateWSStatus(newState);
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
                    dialogDispatcher.dispatchJSON(method, jsonIncome);

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
        String name = data.getString("name");

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
        StringBuilder b = new StringBuilder();
        for (char c : message.getMessageBody().toCharArray()) {
            if (c >= 128)
                b.append("\\u").append(String.format("%04X", (int) c));
            else
                b.append(c);
        }
        String messageBody = b.toString();

        List<AttachedFile> files = message.getFiles();
        Object[] filesParams = (files != null && files.size() > 0) ? {"listIdFile", message.getFiles()} : {};
        WebSocketRequest request =
                new WebSocketRequest("Messages", "sendFirst", profile.getAuthToken(),
                        new Object[][] {
                                {"idUser", uid},
                                {"textMessage", messageBody},
                                ,
                        })
        reconnect();



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

        List<Integer> myUserIDs = new ArrayList(userIDs.size());
        for (String uid: userIDs) {
            myUserIDs.add(Integer.valueOf(uid));
        }
        myUserIDs.add(Integer.valueOf(profile.getUid()));

        WebSocketRequest webSocketRequest =
                new WebSocketRequest("Chats", "create", profile.getAuthToken(),
                        new Object[][]{
                                {"nameChat", title},
                                {"idUsers", myUserIDs}
                        });

        sendRequest(webSocketRequest);
    }

    @Override
    public void write(@NonNull String cid) {
        WebSocketRequest request =
                new WebSocketRequest("Messages", "write", profile.getAuthToken(),
                        new Object[][]{
                                {"idChat", cid}
                        });
        sendRequest(request);
    }

    public void addUser(String uid, String cid) {
        WebSocketRequest request =
                new WebSocketRequest("Chats", "addUser", profile.getAuthToken(),
                        new Object[][] {
                                {"idRoom", cid},
                                {"idUser", uid}
                        });
        sendRequest(request);
    }

    public void updateName(String cid, String chatName) {
        WebSocketRequest request =
                new WebSocketRequest("Chats", "updateName", profile.getAuthToken(),
                        new Object[][] {
                                {"idRoom", cid},
                                {"nameChat", chatName}
                        });
        sendRequest(request);
    }

    public void disconnect() {
        ws.disconnect();
    }

    @Override
    public boolean isConnected() {
        return ws.getState().equals(WebSocketState.OPEN);
    }

    public void setGroupCreateListener(IGroupCreateListener groupCreateListener) {
        this.groupCreateListener = groupCreateListener;
    }

    public void setWsStatusListener(WSStatusListener chatListener) {
        this.wsStatusListener = chatListener;
    }

    public void setGroupEditListener(@Nullable IGroupEditListener groupEditListener) {
        this.groupEditListener = groupEditListener;
    }

    public void setChatListener(@Nullable IChatListener chatListener) {
        dialogDispatcher.setChatListener(chatListener);
    }
}
