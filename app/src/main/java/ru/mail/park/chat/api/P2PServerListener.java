package ru.mail.park.chat.api;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.jivesoftware.smack.proxy.ProxyInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import info.guardianproject.netcipher.NetCipher;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import ru.mail.park.chat.message_interfaces.IMessageReaction;
import ru.mail.park.chat.message_interfaces.IMessageSender;
import ru.mail.park.chat.message_interfaces.Jsonifier;
import ru.mail.park.chat.models.AttachedFile;
import ru.mail.park.chat.models.Message;
import ru.mail.park.chat.models.OwnerProfile;

/**
 * Created by 1запуск BeCompact on 29.02.2016.
 */
@Deprecated
public class P2PServerListener extends Thread implements IMessageSender {
    ServerSocket serverSocket;
    DataInputStream input;
    DataOutputStream output;

    Activity activity;
    IMessageReaction messageListener;
    DestinationParams destination;

    int port = 0;

    public static class DestinationParams {
        public String destinationName;
        public int destinationPort;
    }

    public P2PServerListener(Activity activity, IMessageReaction messageListener, DestinationParams destination) {
        this.activity = activity;
        this.messageListener = messageListener;
        this.destination = destination;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void run() {

    }

    private void publishProgress(final String message) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onProgressUpdate(message);
            }
        });
    }

    protected void onProgressUpdate(String... values) {
        try {
            messageListener.onIncomeMessage(new JSONObject(values[0]));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String chatID, Message message) {
        Log.i("P2P Server OUT message", message.getMessageBody());
        send(message);
    }

    public void sendFirstMessage(String userID, Message message) {
        Log.i("P2P Server OUT message", message.getMessageBody());
        send(message);
    }

    private void send(Message message) {
        if (output != null) {
            try {
                OwnerProfile owner = new OwnerProfile(activity);
                JSONObject msg = Jsonifier.jsonifyForRecieve(message, owner);
                msg.put("idMessage", String.valueOf(System.currentTimeMillis()));
                output.writeUTF(msg.toString());
                if (messageListener != null) {
                    messageListener.onAcknowledgeSendMessage(msg);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        /*try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e){
            Log.e("P2P disconnect", e.getLocalizedMessage());
        }*/
    }
}
