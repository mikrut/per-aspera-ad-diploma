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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import ru.mail.park.chat.models.AttachedFile;
import ru.mail.park.chat.models.Chat;
import ru.mail.park.chat.models.Contact;
import ru.mail.park.chat.models.Message;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
public class Messages extends WSConnection implements IMessageSender {
    @Nullable
    private IWSStatusListener wsStatusListener;

    private Set<IDispatcher> dispatchers = new HashSet<>();
    private @Nullable Handler uiHandler;

    Messages(@NonNull final Context context) throws IOException {
        super(context);
    }

    public void addDispatcher(IDispatcher dispatcher) {
        dispatchers.add(dispatcher);
    }

    public void removeDispatcher(IDispatcher dispatcher) {
        dispatchers.remove(dispatcher);
    }

    public WebSocketState getWsStatus() {
        return ws.getState();
    }

    public IWSStatusNotifier getWsStatusNotifier(@NonNull  Handler uiHandler) {
        this.uiHandler = uiHandler;
        return new IWSStatusNotifier() {
            @Override
            public void setWsStatusListener(IWSStatusListener chatListener) {
                wsStatusListener = chatListener;
            }
        };
    }

    @Override
    protected void dispatchNewState(final WebSocketState newState) {
        super.dispatchNewState(newState);
        if (wsStatusListener != null && uiHandler != null) {
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
        for(IDispatcher dispatcher : dispatchers) {
            dispatcher.dispatchJSON(method, jsonIncome);
        }
        super.dispatchJSON(method, jsonIncome);
    }

    @Override
    public void sendMessage(String cid, Message message) {
        WebSocketRequest request = new WebSocketRequest("Messages", "send", profile.getAuthToken(),
                (message.getFiles() != null && message.getFiles().size() > 0) ?
                new Object[][]{
                        {"idRoom", cid},
                        {"textMessage", message.getMessageBody()},
                        {"uniqueId", message.getUniqueID()},
                        {"listIdFile", message.getFiles()}
                } :
                new Object[][]{
                        {"idRoom", cid},
                        {"textMessage", message.getMessageBody()},
                        {"uniqueId", message.getUniqueID()}
                });
        sendRequest(request);
    }

    @Override
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
        WebSocketRequest request =
                new WebSocketRequest("Messages", "sendFirst", profile.getAuthToken(),
                        (files != null && files.size() > 0) ?
                        new Object[][] {
                                {"idUser", uid},
                                {"textMessage", messageBody},
                                {"listIdFile", files},
                        } :
                        new Object[][]{
                                {"idUser", uid},
                                {"textMessage", messageBody}
                        });
        sendRequest(request);
    }

    public void createGroupChat(String title, List<String> userIDs) {
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

    @Override
    public void disconnect() {
        ws.disconnect();
    }

    @Override
    public boolean isConnected() {
        return ws.getState().equals(WebSocketState.OPEN);
    }
}
